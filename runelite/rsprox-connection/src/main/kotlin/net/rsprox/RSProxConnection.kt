package net.rsprox

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.MapSerializer
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
import java.net.SocketException
import java.nio.file.Path
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

@Singleton
public class RSProxConnection {
    private val log = LoggerFactory.getLogger(RSProxConnection::class.java)

    @Inject
    private lateinit var eventBus: EventBus

    private val input = Input(ByteArray(0))
    private val messageQueue: Queue<Any> = ConcurrentLinkedQueue()
    private val connectedPlugins = ConcurrentHashMap.newKeySet<Class<*>>()
    private val sockets = ConcurrentHashMap<Class<*>, AFUNIXSocket>()

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

    /**
     * Starts a connection with RSProx, and requests a full synchronization of the past state.
     * New plugins shall wait until the next synchronization event occurs, before listening to
     * any normal packets, allowing them to fully catch up to state.
     *
     * Note that it is not possible to stop a connection once started, as multiple plugins could
     * rely on the same connection stream of events.
     *
     * @return true if the connection was successfully established (or is already running),
     * false if unable to connect to RSProx.
     */
    public fun startConnection(pluginClass: Class<*>): Boolean {
        val port =
            System
                .getProperty("sun.java.command")
                .substringAfter("--jav_config=http://127.0.0.1:")
                .substringBefore('/')
                .toInt()
        log.info("RSProx port: $port")
        if (connectedPlugins.add(pluginClass)) {
            return initializeUnixSocket(pluginClass, port)
        }
        return false
    }

    public fun stopConnection(pluginClass: Class<*>): Boolean {
        if (!connectedPlugins.remove(pluginClass)) {
            return false
        }
        eventBus.unregister(this)
        sockets.remove(pluginClass)?.close()
        return true
    }

    private fun Path.withExtension(ext: String): Path {
        val name = this.fileName.toString()
        val base = name.substringBeforeLast('.', name)
        return this.resolveSibling("$base.$ext")
    }

    private fun initializeUnixSocket(
        pluginClass: Class<*>,
        port: Int,
    ): Boolean {
        val root = Path(System.getProperty("user.home"), ".rsprox", "sockets")
        val socketFile = root.resolve("rsprox-tunnel-$port-${pluginClass.simpleName}.con")
        socketFile.deleteIfExists()
        socketFile.createFile()
        // Wait for RSProx side to delete the socket file,
        // which acts as a signal that the server has initialized.
        while (socketFile.exists()) {
            Thread.sleep(20)
        }
        return listenToUnixSocket(
            pluginClass,
            socketFile.withExtension("socket"),
        )
    }

    private fun listenToUnixSocket(
        pluginClass: Class<*>,
        socketFile: Path,
    ): Boolean {
        val address = AFUNIXSocketAddress.of(socketFile)
        val socket = AFUNIXSocket.newInstance()
        try {
            socket.connect(address, 2_500)
        } catch (_: SocketException) {
            log.info("Unable to establish a socket connection to $socketFile")
            return false
        } catch (e: Exception) {
            log.error("Unable to initialize a unix socket to $socketFile", e)
            return false
        }
        eventBus.register(this)
        sockets[pluginClass] = socket
        thread(start = true, isDaemon = true) {
            log.info("Unix Socket connected via $socketFile")
            val inputStream = DataInputStream(socket.inputStream)
            while (true) {
                val next =
                    try {
                        inputStream.readNext()
                    } catch (_: SocketException) {
                        break
                    }
                messageQueue.offer(next)
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

    @Subscribe
    private fun onGameTick(
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
