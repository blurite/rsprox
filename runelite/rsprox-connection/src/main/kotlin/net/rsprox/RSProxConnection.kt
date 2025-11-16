package net.rsprox

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import net.rsprox.events.RSProxPacket
import net.rsprox.events.RSProxSync
import net.runelite.api.events.GameTick
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import org.newsclub.net.unix.AFUNIXSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import org.objenesis.strategy.StdInstantiatorStrategy
import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.SocketException
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread
import kotlin.io.path.Path

@Singleton
public class RSProxConnection {
    private val log = LoggerFactory.getLogger(RSProxConnection::class.java)

    @Inject
    private lateinit var eventBus: EventBus

    private val input = Input(ByteArray(0))
    private val messageQueue: Queue<Any> = ConcurrentLinkedQueue()
    private val sync: AtomicBoolean = AtomicBoolean(false)
    private val running: AtomicBoolean = AtomicBoolean(false)
    private val connected: AtomicBoolean = AtomicBoolean(false)

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

    public fun startConnection(): Boolean {
        connected.set(true)
        sync.set(true)
        if (running.getAndSet(true)) {
            eventBus.register(this)
            return true
        }
        val port =
            System
                .getProperty("sun.java.command")
                .substringAfter("--jav_config=http://127.0.0.1:")
                .substringBefore('/')
                .toInt()
        log.info("RSProx port: $port")
        return initializeUnixSocket(port)
    }

    public fun stopConnection() {
        connected.set(false)
        eventBus.unregister(this)
    }

    private fun initializeUnixSocket(port: Int): Boolean {
        val root = Path(System.getProperty("user.home"), ".rsprox", "sockets")
        val socketFile = root.resolve("rsprox-tunnel-$port.socket")
        val address = AFUNIXSocketAddress.of(socketFile)
        val socket = AFUNIXSocket.newInstance()
        try {
            socket.connect(address, 2_500)
        } catch (_: SocketException) {
            log.info("Unable to establish a socket connection to the target.")
            return false
        } catch (e: Exception) {
            log.error("Unable to initialize a unix socket", e)
            return false
        }
        eventBus.register(this)
        thread(start = true, isDaemon = true) {
            log.info("Unix Socket connected via $socketFile")
            val inputStream = DataInputStream(socket.inputStream)
            val outputStream = DataOutputStream(socket.outputStream)
            var discardUntilSync = false
            while (true) {
                val next = inputStream.readNext()
                if (!connected.get()) {
                    continue
                }
                // When a sync is requested, drop everything else and wait until the sync message comes in
                if (sync.get()) {
                    sync.set(false)
                    messageQueue.clear()
                    discardUntilSync = true
                    outputStream.writeMessage("Sync".toByteArray())
                }
                if (discardUntilSync) {
                    if (next !is RSProxSync) {
                        continue
                    }
                    discardUntilSync = false
                    messageQueue.offer(next)
                    continue
                }

                try {
                    messageQueue.offer(next)
                } catch (_: SocketException) {
                    stopConnection()
                    break
                }
            }
        }
        return true
    }

    private fun DataInputStream.readNext(): Any {
        when (val dirIndicator = readByte().toInt()) {
            FLAG_PACKET -> {
                return RSProxPacket(readMessage())
            }
            FLAG_BEGIN_SYNC -> {
                val packets = ArrayList<Any>()
                while (true) {
                    val nextDir = readByte().toInt()
                    if (nextDir == FLAG_END_SYNC) {
                        return RSProxSync(packets)
                    }
                    packets += readMessage()
                }
            }
            else -> {
                error("Unknown dir indicator: $dirIndicator")
            }
        }
    }

    private fun DataInputStream.readMessage(): Any {
        val length = readInt()
        val data = readNBytes(length)
        input.buffer = data
        return kryo.readClassAndObject(input)
    }

    private fun DataOutputStream.writeMessage(bytes: ByteArray) {
        writeInt(bytes.size)
        write(bytes)
        flush()
    }

    @Subscribe
    public fun onGameTick(
        @Suppress("unused") gameTick: GameTick,
    ) {
        while (true) {
            eventBus.post(messageQueue.poll() ?: return)
        }
    }

    private companion object {
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
