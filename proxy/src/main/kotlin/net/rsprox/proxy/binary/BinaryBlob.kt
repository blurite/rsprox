package net.rsprox.proxy.binary

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.xtea.XteaKey
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.OldSchoolCache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.live.LiveConnectionInfo
import net.rsprox.cache.resolver.LiveCacheResolver
import net.rsprox.protocol.session.AttributeMap
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.config.BINARY_PATH
import net.rsprox.proxy.config.JavConfig
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.proxy.plugin.PluginLoader
import net.rsprox.proxy.transcriber.LiveTranscriberSession
import net.rsprox.proxy.util.NopSessionMonitor
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.transcriber.BaseMessageConsumerContainer
import net.rsprox.transcriber.MessageConsumer
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.Path
import kotlin.time.TimeSource

public data class BinaryBlob(
    public val header: BinaryHeader,
    public val stream: BinaryStream,
    public val writeIntervalSeconds: Int,
    private val monitor: SessionMonitor<BinaryHeader>,
    private val filters: PropertyFilterSetStore,
) {
    private var lastWrite = TimeSource.Monotonic.markNow()
    private var lastWriteSize = 0
    private val closed = AtomicBoolean(false)
    private var liveSession: LiveTranscriberSession? = null

    public fun append(
        direction: StreamDirection,
        packet: ByteBuf,
    ) {
        if (closed.get()) {
            throw IllegalStateException("Binary stream is closed.")
        }
        liveSession?.pass(direction, packet.copy())
        stream.append(
            direction,
            packet,
        )
        val interval = writeIntervalSeconds
        if (interval != 0) {
            val elapsed = lastWrite.elapsedNow().inWholeSeconds
            if (elapsed >= interval) {
                lastWrite = TimeSource.Monotonic.markNow()
                write()
            }
        }
    }

    public fun close() {
        val isClosed = closed.getAndSet(true)
        if (isClosed) {
            return
        }
        write()
        liveSession?.shutdown()
        this.monitor.onLogout(header)
    }

    private fun write() {
        write(BINARY_PATH.resolve(header.fileName()))
    }

    private fun write(path: Path) {
        val file = path.toFile()
        if (file.isDirectory) {
            throw IllegalArgumentException("Path does not point to a file: $path")
        }
        val header = header.encode(ByteBufAllocator.DEFAULT).buffer
        val streamCopy = stream.copy()
        val headerBytes = header.readableBytes()
        val streamBytes = streamCopy.readableBytes()
        val array = ByteArray(headerBytes + streamBytes)
        header.readBytes(array, 0, headerBytes)
        streamCopy.readBytes(array, headerBytes, streamBytes)
        val parent = path.parent
        val tempFileName = ".${file.name}"
        val tempPath =
            if (parent == null) {
                val root = path.root
                if (root == null) {
                    Path(tempFileName)
                } else {
                    root.resolve(tempFileName)
                }
            } else {
                parent.resolve(tempFileName)
            }
        checkNotNull(tempPath) {
            "Temporary resolved file is null: $path @ $tempFileName"
        }
        try {
            Files.write(
                tempPath,
                array,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.SYNC,
            )
        } catch (t: Throwable) {
            logger.error(t) {
                "Unable to write buffer to file $tempPath"
            }
            return
        }
        try {
            Files.move(tempPath, path, StandardCopyOption.ATOMIC_MOVE)
            logger.info {
                "Appended ${array.size - lastWriteSize} bytes to $path"
            }
            lastWriteSize = array.size
        } catch (t: Throwable) {
            logger.error(t) {
                "Unable to copy temporary binary file to real file: $tempPath"
            }
        }
    }

    private fun loadJavConfig(): JavConfig? {
        val url = "https://oldschool.runescape.com/jav_config.ws"
        return runCatching {
            val config = JavConfig(URL(url))
            logger.debug { "Jav config loaded from $url" }
            config
        }.getOrNull()
    }

    public fun hookLiveTranscriber(
        key: XteaKey,
        pluginLoader: PluginLoader,
    ) {
        check(this.liveSession == null) {
            "Live session already hooked."
        }
        check(this.stream.isEmpty()) {
            "Stream has already been launched - it is impossible to hook mid-session."
        }
        try {
            val masterIndex =
                Js5MasterIndex.trimmed(
                    header.revision,
                    header.js5MasterIndex,
                )
            val javConfig = loadJavConfig()
            val host =
                javConfig
                    ?.getCodebase()
                    ?.removePrefix("http://")
                    ?.removePrefix("https://")
                    ?.removeSuffix("/")
                    ?: "oldschool67.runescape.com"
            val info =
                LiveConnectionInfo(
                    host,
                    PORT,
                    header.revision,
                    key,
                    masterIndex,
                )
            val provider =
                CacheProvider {
                    OldSchoolCache(LiveCacheResolver(info), masterIndex)
                }
            pluginLoader.loadTranscriberPlugins("osrs", provider)
            val latestPlugin = pluginLoader.getPluginOrNull(header.revision)
            if (latestPlugin == null) {
                logger.info { "Plugin for ${header.revision} missing, no live transcriber hooked." }
                return
            }
            val transcriberProvider = pluginLoader.getTranscriberProvider(header.revision)
            val consumers = BaseMessageConsumerContainer(listOf(MessageConsumer.STDOUT_CONSUMER))
            val session = Session(header.localPlayerIndex, AttributeMap())
            val decodingSession = DecodingSession(this, latestPlugin)
            val runner =
                transcriberProvider.provide(
                    consumers,
                    provider,
                    monitor,
                    filters,
                )
            this.liveSession =
                LiveTranscriberSession(
                    session,
                    decodingSession,
                    runner,
                )
        } catch (t: Throwable) {
            logger.error(t) {
                "Unable to hook a live transcriber."
            }
        }
    }

    public companion object {
        private const val PORT: Int = 43_594
        private val logger = InlineLogger()

        public fun decode(
            path: Path,
            filters: PropertyFilterSetStore,
        ): BinaryBlob {
            val file = path.toFile()
            if (!file.isFile) {
                throw IllegalArgumentException("Path does not point to a file: $path")
            }
            val buffer = Unpooled.wrappedBuffer(file.readBytes())
            val header = BinaryHeader.decode(buffer.toJagByteBuf())
            val stream = BinaryStream(buffer.slice())
            return BinaryBlob(header, stream, 0, NopSessionMonitor, filters)
        }
    }
}
