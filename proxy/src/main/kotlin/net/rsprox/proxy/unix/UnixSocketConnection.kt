package net.rsprox.proxy.unix

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.game.outgoing.model.map.RebuildLogin
import net.rsprox.proxy.connection.ProxyConnectionContainer
import org.newsclub.net.unix.AFUNIXServerSocket
import org.newsclub.net.unix.AFUNIXSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import org.objenesis.strategy.StdInstantiatorStrategy
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.SocketException
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

public class UnixSocketConnection(
    private val socketFile: Path,
    private val connections: ProxyConnectionContainer,
) {
    private lateinit var unixListenerThread: Thread
    private lateinit var eventListenerThread: Thread
    private val packets = ConcurrentLinkedQueue<UnixPacket>()
    private val eventQueue = LinkedBlockingQueue<Event>()
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
        }

    private val msgOutput = Output(4096, -1)
    private val running: AtomicBoolean = AtomicBoolean(true)

    public fun start() {
        check(!this::unixListenerThread.isInitialized) {
            "Unix socket already initialized."
        }

        val address = AFUNIXSocketAddress.of(socketFile)
        val server = AFUNIXServerSocket.newInstance()

        server.bind(address)
        logger.debug { "Waiting for RSProx-Connection plugin to establish a connection." }

        this.unixListenerThread =
            thread(start = true, isDaemon = true) {
                server.use { srv ->
                    val socket = srv.accept()
                    logger.debug { "RSProx-Connection plugin has successfully established a connection." }
                    beginEventListening(socket)

                    val input = DataInputStream(socket.inputStream)

                    while (running.get()) {
                        try {
                            when (val request = String(input.readMessage())) {
                                "Sync" -> {
                                    eventQueue.offer(UnixSyncEvent)
                                }
                                else -> {
                                    logger.warn { "Unknown request: $request" }
                                }
                            }
                        } catch (_: SocketException) {
                            closeConnection()
                            break
                        }
                    }
                }
            }
    }

    private fun beginEventListening(socket: AFUNIXSocket) {
        this.eventListenerThread =
            thread(start = true, isDaemon = true) {
                val outputStream = DataOutputStream(socket.outputStream)
                while (running.get()) {
                    try {
                        when (val event = eventQueue.take()) {
                            is UnixPacket -> {
                                outputStream.writeMessage(event)
                            }
                            UnixSyncEvent -> {
                                outputStream.writeIndicator(FLAG_BEGIN_SYNC)
                                for (packet in packets) {
                                    outputStream.writeMessage(packet)
                                }
                                outputStream.writeIndicator(FLAG_END_SYNC)
                            }
                        }
                    } catch (_: SocketException) {
                        closeConnection()
                        break
                    } catch (e: Exception) {
                        logger.error(e) {
                            "Unable to process event."
                        }
                    }
                }
            }
    }

    public fun push(payload: IncomingMessage) {
        if (payload is RebuildLogin) {
            packets.clear()
        }
        val packet = UnixPacket(payload)
        packets.add(packet)
        eventQueue.offer(packet)
    }

    private fun DataInputStream.readMessage(): ByteArray {
        val len = readInt()
        return ByteArray(len).also { readFully(it) }
    }

    private fun DataOutputStream.writeMessage(unixPacket: UnixPacket) {
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

    private fun closeConnection() {
        running.set(false)
        packets.clear()
        eventQueue.clear()
        connections.removeUnixConnection(this)
    }

    private sealed interface Event

    private data object UnixSyncEvent : Event

    private class UnixPacket(
        val message: IncomingMessage,
    ) : Event

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
    }
}
