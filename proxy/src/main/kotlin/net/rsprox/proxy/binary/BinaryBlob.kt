package net.rsprox.proxy.binary

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import net.rsprox.proxy.plugin.DecoderLoader
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.proxy.transcriber.LiveTranscriberSession
import net.rsprox.proxy.util.NopSessionMonitor
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.indexing.NopBinaryIndex
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.state.SessionState
import net.rsprox.transcriber.state.SessionTracker
import net.rsprox.transcriber.text.TextMessageConsumerContainer
import net.rsprox.transcriber.text.TextTranscriberProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.time.TimeSource

public data class BinaryBlob(
    public val header: BinaryHeader,
    public val stream: BinaryStream,
    public val writeIntervalSeconds: Int,
    private val monitor: SessionMonitor<BinaryHeader>,
    private val filters: PropertyFilterSetStore,
    private val settings: SettingSetStore,
    private val liveOldschoolConnection: Boolean,
) {
    private var lastWrite = TimeSource.Monotonic.markNow()
    private var lastWriteSize = 0
    private val closed = AtomicBoolean(false)
    private var liveSession: LiveTranscriberSession? = null
    private var binaryFolder: String? = null
    private var lastBandwidthUpdate = TimeSource.Monotonic.markNow()
    private var lastOutgoingBytes: Int = 0
    private var lastIncomingBytes: Int = 0

    public fun liveCache(): CacheProvider {
        return liveSession?.cacheProvider ?: error("Cache unavailable.")
    }

    public fun append(
        direction: StreamDirection,
        packet: ByteBuf,
    ) {
        if (closed.get()) {
            throw IllegalStateException("Binary stream is closed.")
        }
        val bytes = packet.readableBytes()
        if (direction == StreamDirection.SERVER_TO_CLIENT) {
            lastIncomingBytes += bytes
        } else {
            lastOutgoingBytes += bytes
        }
        val elapsedMillis = lastBandwidthUpdate.elapsedNow().inWholeMilliseconds
        if (elapsedMillis >= 1000) {
            val incomingBytesPerSecond = lastIncomingBytes * 1000 / elapsedMillis
            val outgoingBytesPerSecond = lastOutgoingBytes * 1000 / elapsedMillis
            monitor.onIncomingBytesPerSecondUpdate(incomingBytesPerSecond)
            monitor.onOutgoingBytesPerSecondUpdate(outgoingBytesPerSecond)
            this.lastIncomingBytes = 0
            this.lastOutgoingBytes = 0
            this.lastBandwidthUpdate = TimeSource.Monotonic.markNow()
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
        liveSession?.flush()
        this.lastIncomingBytes = 0
        this.lastOutgoingBytes = 0
        this.monitor.onIncomingBytesPerSecondUpdate(-1)
        this.monitor.onOutgoingBytesPerSecondUpdate(-1)
        this.monitor.onLogout(header)
        if (liveOldschoolConnection) {
            try {
                submitKeys()
            } catch (e: Exception) {
                logger.error(e) {
                    "Exception while submitting keys"
                }
            }
        }
    }

    private fun submitKeys() {
        val keys =
            liveSession
                ?.getKeyStorage()
                ?.get() ?: return
        if (keys.isEmpty()) {
            logger.info { "No keys to submit." }
            return
        }
        logger.info { "Submitting ${keys.size} XTEA keys to OpenRS2." }
        val keysArray = keys.values.distinct().toTypedArray()
        val request =
            Request
                .Builder()
                .post(JACKSON.writeValueAsString(keysArray).toRequestBody(JSON))
                .url(XTEA_KEYS_ENDPOINT)
                .build()
        CLIENT.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    logger.error(e) {
                        "XTEA key submission failed."
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    response.use {
                        if (it.isSuccessful) {
                            logger.info { "XTEA key submission successful." }
                        } else {
                            logger.error {
                                "XTEA key submission failed with status code ${it.code}."
                            }
                        }
                    }
                }
            },
        )
    }

    public fun shutdown() {
        liveSession?.shutdown()
    }

    public fun reopen() {
        liveSession?.flush()
        closed.set(false)
        this.monitor.onLogin(header)
    }

    private fun write() {
        val folder = binaryFolder ?: return
        val directory = BINARY_PATH.resolve(folder)
        directory.createDirectories()
        val path = directory.resolve(header.fileName())
        write(path)
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
        try {
            header.readBytes(array, 0, headerBytes)
        } finally {
            header.release()
        }
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

    public fun hookLiveTranscriber(
        key: XteaKey,
        decoderLoader: DecoderLoader,
        binaryFolder: String?,
    ) {
        check(this.liveSession == null) {
            "Live session already hooked."
        }
        check(this.stream.isEmpty()) {
            "Stream has already been launched - it is impossible to hook mid-session."
        }
        try {
            this.binaryFolder = binaryFolder
            val masterIndex =
                Js5MasterIndex.trimmed(
                    header.revision,
                    header.js5MasterIndex,
                )

            val info =
                LiveConnectionInfo(
                    header.worldHost,
                    PORT,
                    header.revision,
                    key,
                    masterIndex,
                )
            val cache = OldSchoolCache(LiveCacheResolver(info), masterIndex)
            val provider =
                CacheProvider {
                    cache
                }
            monitor.onCacheUpdate(provider)
            decoderLoader.load(provider, header.revision)
            val latestPlugin = decoderLoader.getDecoderOrNull(header.revision, provider)
            if (latestPlugin == null) {
                logger.info { "Plugin for ${header.revision} missing, no live transcriber hooked." }
                return
            }
            val transcriberProvider = TextTranscriberProvider()
            val consumers = TextMessageConsumerContainer(emptyList())
            val session = Session(header.localPlayerIndex, AttributeMap())
            val decodingSession = DecodingSession(this, latestPlugin)
            val state = SessionState(header.revision, settings)
            val runner =
                transcriberProvider.provide(
                    consumers,
                    provider,
                    monitor,
                    filters,
                    settings,
                    NopBinaryIndex,
                    state,
                )
            val sessionTracker =
                SessionTracker(
                    state,
                    provider.get(),
                    monitor,
                )
            val liveSession =
                LiveTranscriberSession(
                    session,
                    decodingSession,
                    runner,
                    sessionTracker,
                    provider,
                )
            this.liveSession = liveSession
            liveSession.setRevision(header.revision)
        } catch (t: Throwable) {
            logger.error(t) {
                "Unable to hook a live transcriber."
            }
        }
    }

    public companion object {
        private const val PORT: Int = 43_594
        private const val XTEA_KEYS_ENDPOINT: String = "https://archive.openrs2.org/keys"
        private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        private val JACKSON = jacksonObjectMapper()
        private val CLIENT = OkHttpClient()
        private val logger = InlineLogger()

        public fun decode(
            path: Path,
            filters: PropertyFilterSetStore,
            settings: SettingSetStore,
        ): BinaryBlob {
            val file = path.toFile()
            if (!file.isFile) {
                throw IllegalArgumentException("Path does not point to a file: $path")
            }
            return decode(file.readBytes(), filters, settings)
        }

        public fun decode(
            buf: ByteArray,
            filters: PropertyFilterSetStore,
            settings: SettingSetStore,
        ): BinaryBlob {
            val buffer = Unpooled.wrappedBuffer(buf)
            val header = BinaryHeader.decode(buffer.toJagByteBuf())
            val stream = BinaryStream(buffer.slice())
            return BinaryBlob(
                header,
                stream,
                0,
                NopSessionMonitor,
                filters,
                settings,
                false,
            )
        }
    }
}
