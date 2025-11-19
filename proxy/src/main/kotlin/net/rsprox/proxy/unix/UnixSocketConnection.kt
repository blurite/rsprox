package net.rsprox.proxy.unix

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.MapSerializer
import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.game.outgoing.model.map.RebuildLogin
import net.rsprox.proxy.config.SOCKETS_DIRECTORY
import org.newsclub.net.unix.AFUNIXServerSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import org.objenesis.strategy.StdInstantiatorStrategy
import java.io.DataOutputStream
import java.net.SocketException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.name

public class UnixSocketConnection(
    private val port: Int,
) {
    private lateinit var socketConnectionListener: Thread
    private val eventLog: EventLog = EventLog()
    private val uniqueConnections = ConcurrentHashMap.newKeySet<Path>()
    private val kryo =
        Kryo().apply {
            isRegistrationRequired = false
            instantiatorStrategy = StdInstantiatorStrategy()

            val collectionClasses =
                listOf(
                    "kotlin.collections.builders.ListBuilder",
                    "kotlin.collections.ArrayDeque",
                )

            for (className in collectionClasses) {
                val clazz = Class.forName(className)
                @Suppress("UNCHECKED_CAST")
                addDefaultSerializer(
                    clazz as Class<Any>,
                    collectionSerializer as Serializer<Any>,
                )
            }

            val mapBuilderClass = Class.forName("kotlin.collections.builders.MapBuilder")
            @Suppress("UNCHECKED_CAST")
            addDefaultSerializer(
                mapBuilderClass as Class<Any>,
                mapSerializer as Serializer<Any>,
            )
        }

    public fun start() {
        check(!this::socketConnectionListener.isInitialized) {
            "Unix socket already initialized."
        }

        this.socketConnectionListener =
            thread(start = true, isDaemon = true) {
                watchDirectory(SOCKETS_DIRECTORY) { path ->
                    if (!path.name.startsWith("rsprox-tunnel-$port-")) {
                        return@watchDirectory
                    }
                    if (path.extension != "con") return@watchDirectory
                    val socketPath = path.withExtension("socket")
                    if (!uniqueConnections.add(socketPath)) {
                        return@watchDirectory
                    }
                    createUnixServer(path, socketPath)
                }
            }
    }

    private fun Path.withExtension(ext: String): Path {
        val name = this.fileName.toString()
        val base = name.substringBeforeLast('.', name)
        return this.resolveSibling("$base.$ext")
    }

    private fun watchDirectory(
        path: Path,
        onCreate: (Path) -> Unit,
    ) {
        val watchService = FileSystems.getDefault().newWatchService()
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)

        while (true) {
            val key = watchService.take()

            for (event in key.pollEvents()) {
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    val createdFile = path.resolve(event.context() as Path)
                    onCreate(createdFile)
                }
            }

            if (!key.reset()) {
                break
            }
        }
    }

    private fun createUnixServer(
        conFile: Path,
        socketFile: Path,
    ) {
        val address = AFUNIXSocketAddress.of(socketFile)
        val server = AFUNIXServerSocket.newInstance()

        server.bind(address)
        conFile.deleteIfExists()
        logger.debug { "Waiting for RSProx-Connection to establish a connection to $socketFile." }
        thread(start = true, isDaemon = true) {
            server.use { srv ->
                val socket = srv.accept()
                logger.debug { "RSProx-Connection has successfully established a connection to $socketFile." }
                val outputStream = DataOutputStream(socket.outputStream)
                val msgOutput = Output(4096, -1)
                val key = eventLog.consumerKey()
                eventLog.addConsumer(
                    key,
                    syncBlock = {
                        try {
                            val snapshot = eventLog.snapshot(0)
                            outputStream.writeIndicator(FLAG_BEGIN_SYNC)
                            for (packet in snapshot) {
                                outputStream.writeMessage(msgOutput, packet)
                            }
                            outputStream.writeIndicator(FLAG_END_SYNC)
                        } catch (_: SocketException) {
                            eventLog.removeConsumer(key)
                            uniqueConnections.remove(socketFile)
                        }
                    },
                    consumer = { event ->
                        try {
                            outputStream.writeMessage(msgOutput, event)
                        } catch (_: SocketException) {
                            eventLog.removeConsumer(key)
                            uniqueConnections.remove(socketFile)
                        }
                    },
                )
            }
        }
    }

    public fun push(payload: IncomingMessage) {
        if (payload is RebuildLogin) {
            eventLog.clearEvents()
        }
        eventLog.append(UnixPacket(payload))
    }

    private fun DataOutputStream.writeMessage(
        msgOutput: Output,
        unixPacket: UnixPacket,
    ) {
        msgOutput.setPosition(0)
        kryo.writeClassAndObject(msgOutput, unixPacket.message)
        msgOutput.flush()
        val bytes = msgOutput.buffer
        val length = msgOutput.position()

        writeByte(FLAG_PACKET)
        writeInt(length)
        write(bytes, 0, length)
        flush()
    }

    private fun DataOutputStream.writeIndicator(value: Int) {
        writeByte(value)
        flush()
    }

    private sealed interface Event

    private class UnixPacket(
        val message: IncomingMessage,
    ) : Event

    private class EventLog {
        private val lock = ReentrantLock()
        private val events = mutableListOf<UnixPacket>()
        private val consumers = ConcurrentHashMap<Int, (UnixPacket) -> Unit>()
        private val consumerKeyCount: AtomicInteger = AtomicInteger(0)

        fun consumerKey(): Int {
            return consumerKeyCount.incrementAndGet()
        }

        fun addConsumer(
            key: Int,
            syncBlock: () -> Unit,
            consumer: (event: UnixPacket) -> Unit,
        ) {
            withLock {
                syncBlock()
                consumers.put(key, consumer)
            }
        }

        fun removeConsumer(key: Int) {
            withLock {
                consumers.remove(key)
            }
        }

        fun clearEvents() {
            withLock {
                events.clear()
            }
        }

        fun append(event: UnixPacket) {
            return withLock {
                events.add(event)
                for ((_, consumer) in consumers) {
                    consumer(event)
                }
            }
        }

        fun snapshot(fromIndex: Int = 0): List<UnixPacket> {
            return withLock {
                if (fromIndex >= events.size) {
                    emptyList()
                } else {
                    events.subList(fromIndex, events.size)
                }
            }
        }

        fun size(): Int {
            return withLock {
                events.size
            }
        }

        private inline fun <T> withLock(block: () -> T): T {
            lock.lock()
            return try {
                block()
            } finally {
                lock.unlock()
            }
        }
    }

    private companion object {
        private val logger = InlineLogger()
        private const val FLAG_PACKET: Int = 0
        private const val FLAG_BEGIN_SYNC: Int = 1
        private const val FLAG_END_SYNC: Int = 2

        private val collectionSerializer =
            object : CollectionSerializer<MutableCollection<Any?>>() {
                override fun create(
                    kryo: Kryo,
                    input: Input?,
                    type: Class<out MutableCollection<Any?>>?,
                    size: Int,
                ): MutableCollection<Any?> = ArrayList(size)
            }.apply {
                setElementsCanBeNull(true)
            }

        private val mapSerializer =
            object : MapSerializer<LinkedHashMap<Any?, Any?>>() {
                @Suppress("UNCHECKED_CAST")
                override fun create(
                    kryo: Kryo,
                    input: Input?,
                    type: Class<out LinkedHashMap<Any?, Any?>>?,
                    size: Int,
                ): LinkedHashMap<Any?, Any?> {
                    return LinkedHashMap(size)
                }
            }
    }
}
