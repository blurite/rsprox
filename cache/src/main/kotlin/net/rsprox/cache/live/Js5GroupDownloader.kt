package net.rsprox.cache.live

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.handler.codec.DecoderException
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toByteArray
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.util.CacheGroupRequest

/**
 * A sequential blocking JS5 group downloader. This is not intended for downloading full caches,
 * but rather for obtaining individual singular groups. This downloader blocks caller thread with each
 * [get] operation performed, with a timeout of 25 seconds. The same downloader instance can be used for
 * extended periods of time, as it will establish a new connection automatically if the old one has timed out.
 */
public class Js5GroupDownloader internal constructor(
    private val bootstrapFactory: Js5BootstrapFactory,
    private val info: LiveConnectionInfo,
) {
    private lateinit var bootstrap: Bootstrap
    private lateinit var channel: Channel
    private var connectionStatus: Int = CONNECTION_STATUS_UNINITIALIZED
    private val responses: MutableMap<Int, ByteBuf> = mutableMapOf()

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lock: Object = Object()

    private fun connect(): Channel {
        if (!this::bootstrap.isInitialized) {
            this.bootstrap = this.bootstrapFactory.createClientBootstrap(this)
        }
        logger.info { "Connecting to /${info.host}:${info.port}" }
        val future = bootstrap.connect(info.host, info.port).sync()
        logger.info { "Successfully connected to /${info.host}:${info.port}" }
        return future.channel()
    }

    public fun isUpToDate(masterIndex: Js5MasterIndex): Boolean {
        return info.masterIndex == masterIndex
    }

    private fun invalidateConnection() {
        if (!this::channel.isInitialized || !this.channel.isActive) {
            this.channel = connect()
            initializeConnection()
        }
    }

    private fun initializeConnection() {
        // Send the normal requests that the client would always make, just in case
        // there's some potential fingerprinting going on, this ensures we appear identical
        // to any regular client.
        js5connect()
    }

    private fun js5connect() {
        val buffer = Unpooled.buffer(21).toJagByteBuf()
        buffer.p1(INIT_JS5REMOTE_CONNECTION)
        buffer.p4(info.revision)
        for (key in info.key.key) {
            buffer.p4(key)
        }
        logger.debug { "Writing JS5 remote connection" }
        writeAndFlush(buffer)
        logger.debug { "Awaiting JS5 remote connection response" }
        await()
        logger.debug { "JS5 remote connection response received: ${this.connectionStatus}" }
        if (this.connectionStatus != CONNECTION_STATUS_SUCCESS) {
            throw DecoderException("Unable to successfully connect to JS5")
        }
        logger.debug { "Requesting master archive" }
        val masterIndex = get(0xFF, 0xFF)
        logger.debug { "Master archive received: $masterIndex" }
        check(info.masterIndex.data contentEquals masterIndex.toByteArray()) {
            "Response contains a different master index from expectations"
        }
        masterIndex.release()
    }

    internal fun resumeConnection(success: Boolean) {
        this.connectionStatus =
            if (success) {
                CONNECTION_STATUS_SUCCESS
            } else {
                CONNECTION_STATUS_FAILURE
            }
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    internal fun groupResponse(
        archive: Int,
        group: Int,
        buffer: ByteBuf,
    ) {
        this.responses[pack(archive, group)] = buffer
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    private fun writeAndFlush(buffer: JagByteBuf): ChannelFuture {
        return this.channel.writeAndFlush(buffer.buffer)
    }

    private fun write(buffer: JagByteBuf): ChannelFuture {
        return this.channel.write(buffer.buffer)
    }

    private fun flush() {
        this.channel.flush()
    }

    public fun get(
        archive: Int,
        group: Int,
        urgent: Boolean = true,
    ): ByteBuf {
        invalidateConnection()
        this.responses.clear()
        val buffer = Unpooled.buffer(4).toJagByteBuf()
        buffer.p1(if (urgent) 1 else 0)
        buffer.p1(archive)
        buffer.p2(group)
        writeAndFlush(buffer)
        await()
        // Return the response, while also skipping the archive id & group id header
        return this.responses.getValue(pack(archive, group))
    }

    public fun getBulk(requests: List<CacheGroupRequest>): Map<CacheGroupRequest, ByteBuf> {
        invalidateConnection()
        this.responses.clear()
        for (request in requests) {
            val buffer = Unpooled.buffer(4).toJagByteBuf()
            buffer.p1(if (request.urgent) 1 else 0)
            buffer.p1(request.archive)
            buffer.p2(request.group)
            write(buffer)
        }
        flush()
        awaitBulk(requests.size)
        // Return the responses, while also skipping the archive id & group id header
        return requests.associateWith { this.responses.getValue(pack(it.archive, it.group)) }
    }

    private fun pack(
        archive: Int,
        group: Int,
    ): Int {
        return archive or (group shl 8)
    }

    private fun await() {
        synchronized(lock) {
            lock.wait(25_000)
        }
    }

    private fun awaitBulk(count: Int) {
        var lastCount = 0
        while (true) {
            // Once we have them all, break out of waiting
            if (this.responses.size >= count) {
                break
            }
            // Wait for a single group response for up to 5 seconds
            synchronized(lock) {
                lock.wait(5_000)
            }
            // If the responses size is still the same as it was before the wait call,
            // we couldn't get a response, and as such, break out of the loop
            if (this.responses.size == lastCount) {
                break
            }
            lastCount = this.responses.size
        }
    }

    public fun close() {
        if (this::channel.isInitialized && this.channel.isActive) {
            logger.debug { "Shutting JS5 downloader down" }
            this.channel.close()
        }
    }

    private companion object {
        private const val INIT_JS5REMOTE_CONNECTION: Int = 15
        private const val CONNECTION_STATUS_UNINITIALIZED: Int = -1
        private const val CONNECTION_STATUS_SUCCESS: Int = 0
        private const val CONNECTION_STATUS_FAILURE: Int = 1
        private val logger = InlineLogger()
    }
}
