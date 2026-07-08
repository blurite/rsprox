package net.rsprox.proxy.replay

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import net.rsprot.protocol.Prot
import net.rsprox.cache.store.GroupStore
import net.rsprox.proxy.channel.getServerToClientStreamCipher
import net.rsprox.proxy.plugin.RevisionDecoder
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

public class ReplaySession(
    public val timeline: ReplayTimeline,
    public val revisionDecoder: RevisionDecoder,
    public val cacheStore: GroupStore,
    private val scheduler: ReplayScheduler = ExecutorReplayScheduler(),
) : AutoCloseable {
    private val clientChannel: AtomicReference<Channel?> = AtomicReference(null)
    private val clientLaunchStarted: AtomicBoolean = AtomicBoolean(false)
    private val clientDisconnected: AtomicBoolean = AtomicBoolean(false)
    private val rebuildInProgress: AtomicBoolean = AtomicBoolean(false)
    private val rebuildLock: Any = Any()
    private val keepaliveLock: Any = Any()
    private var pendingReconnectRebuild: PendingReconnectRebuild? = null
    private var pendingReconnectAttach: PendingReconnectAttach? = null
    private var pausedKeepaliveTask: ReplayScheduledTask? = null
    private val initialRebuildNormalFrameIndex: Int? by lazy {
        timeline.frames
            .indexOfFirst { it.prot.isReplayRebuildNormal() }
            .takeIf { it >= 0 }
    }
    private val sendPingProt by lazy {
        revisionDecoder.gameServerProtProvider
            .allProts()
            .filterIsInstance<Prot>()
            .firstOrNull { it.isReplaySendPing() }
    }
    private val packetGroupStartProt by lazy {
        revisionDecoder.gameServerProtProvider
            .allProts()
            .filterIsInstance<Prot>()
            .firstOrNull { it.isReplayPacketGroupStart() }
    }
    public val controller: ReplayPlaybackController =
        ReplayPlaybackController(
            timeline,
            scheduler,
            sink = sink@{ frame ->
                sendReplayFrame(frame)
            },
            fastForwardSink = { frames ->
                sendFastForwardFrames(frames)
            },
            onFinished = {
                logoutClient()
            },
        )

    public fun tryReserveClientLaunch(): Boolean {
        val reserved = clientLaunchStarted.compareAndSet(false, true)
        if (reserved) {
            clientDisconnected.set(false)
        }
        return reserved
    }

    public fun clearClientLaunchReservation() {
        clientLaunchStarted.set(false)
        clientDisconnected.set(false)
    }

    public fun handleLaunchedClientProcessExit() {
        if (hasActiveClient()) {
            return
        }
        synchronized(rebuildLock) {
            pendingReconnectRebuild = null
            pendingReconnectAttach = null
        }
        rebuildInProgress.set(false)
        clientLaunchStarted.set(false)
        clientDisconnected.set(true)
        cancelPausedKeepalive()
    }

    public fun hasClientLaunchStarted(): Boolean {
        return clientLaunchStarted.get()
    }

    public fun hasClientDisconnected(): Boolean {
        return clientDisconnected.get()
    }

    public fun hasActiveClient(): Boolean {
        return clientChannel.get()?.isActive == true
    }

    public fun isRebuildInProgress(): Boolean {
        return rebuildInProgress.get()
    }

    public fun attachClientChannel(channel: Channel) {
        clientChannel.getAndSet(channel)?.takeIf { it != channel && it.isActive }?.close()
        clientDisconnected.set(false)
        registerClientCloseListener(channel)
        val rebuild =
            synchronized(rebuildLock) {
                pendingReconnectRebuild.also {
                    pendingReconnectRebuild = null
                }
            }
        if (rebuild != null) {
            try {
                controller.stop()
                controller.fastForwardToTick(rebuild.targetTick, rebuild.resumeAfter)
            } finally {
                rebuildInProgress.set(false)
                refreshPausedKeepalive()
            }
        } else {
            controller.play()
            refreshPausedKeepalive()
        }
    }

    public fun shouldTreatReconnectAsCleanLogin(): Boolean {
        return synchronized(rebuildLock) {
            pendingReconnectRebuild?.response == ReconnectResponse.LOGIN_OK
        }
    }

    public fun prepareCleanLoginFromCurrentReconnect() {
        val snapshot = controller.snapshot()
        synchronized(rebuildLock) {
            pendingReconnectRebuild =
                PendingReconnectRebuild(
                    targetTick = snapshot.currentTick,
                    resumeAfter = snapshot.state == ReplayPlaybackState.PLAYING,
                    response = ReconnectResponse.LOGIN_OK,
                )
        }
        rebuildInProgress.set(true)
        controller.beginSeeking()
        refreshPausedKeepalive()
    }

    public fun prepareReconnectBootstrap(): ByteArray? {
        val snapshot = controller.snapshot()
        val resumeAfter = snapshot.state == ReplayPlaybackState.PLAYING
        val payload =
            controller.prepareReconnectBootstrapFromCurrent { frame ->
                frame.reconnectBootstrap(timeline.header.localPlayerIndex)
            } ?: return null
        synchronized(rebuildLock) {
            pendingReconnectAttach = PendingReconnectAttach(resumeAfter)
        }
        return payload
    }

    public fun attachReconnectClientChannel(channel: Channel) {
        clientChannel.getAndSet(channel)?.takeIf { it != channel && it.isActive }?.close()
        clientDisconnected.set(false)
        registerClientCloseListener(channel)
        val attach =
            synchronized(rebuildLock) {
                pendingReconnectAttach.also {
                    pendingReconnectAttach = null
                }
            }
        if (attach?.resumeAfter == true) {
            rebuildInProgress.set(false)
            controller.play()
        } else {
            rebuildInProgress.set(false)
            controller.pause()
        }
        refreshPausedKeepalive()
    }

    public fun play() {
        cancelPausedKeepalive()
        controller.play()
        refreshPausedKeepalive()
    }

    public fun pause() {
        controller.pause()
        refreshPausedKeepalive()
    }

    public fun stop() {
        cancelPausedKeepalive()
        val channel = clientChannel.get()
        if (channel != null && channel.isActive) {
            forceReconnectForRebuild(targetTick = 0, resumeAfter = false)
        } else {
            rebuildInProgress.set(false)
            controller.stop()
            refreshPausedKeepalive()
        }
    }

    public fun seekToTick(tick: Int) {
        val snapshot = controller.snapshot()
        val targetTick = tick.coerceIn(0, snapshot.totalTicks)
        if (targetTick == snapshot.currentTick) {
            return
        }
        val resumeAfter = snapshot.state == ReplayPlaybackState.PLAYING
        val channel = clientChannel.get()
        if (channel == null || !channel.isActive) {
            controller.seekToTick(targetTick)
            refreshPausedKeepalive()
            return
        }
        if (targetTick > snapshot.currentTick) {
            controller.fastForwardToTick(targetTick, resumeAfter)
        } else {
            forceReconnectForRebuild(targetTick, resumeAfter)
        }
        refreshPausedKeepalive()
    }

    public fun stepForwardTick(): Int {
        return controller.stepForwardTick().also {
            refreshPausedKeepalive()
        }
    }

    public fun stepBackTick(): Int {
        val target = maxOf(0, controller.snapshot().currentTick - 1)
        seekToTick(target)
        return controller.snapshot().currentTick
    }

    public fun setSpeed(multiplier: Double) {
        controller.setSpeed(multiplier)
    }

    public fun mapBuildComplete() {
        controller.mapBuildComplete()
        refreshPausedKeepalive()
    }

    public fun snapshot(): ReplayPlaybackSnapshot {
        return controller.snapshot()
    }

    override fun close() {
        cancelPausedKeepalive()
        rebuildInProgress.set(false)
        controller.stop()
        logoutClient()
        (cacheStore as? AutoCloseable)?.close()
    }

    private fun logoutClient() {
        cancelPausedKeepalive()
        clientChannel.getAndSet(null)?.takeIf { it.isActive }?.close()
    }

    private fun sendReplayFrame(frame: ReplayFrame) {
        val channel =
            clientChannel.get()
                ?: return
        if (!channel.isActive) {
            return
        }
        val encoded = ReplayPacketEncoder.encode(channel.alloc(), channel.getServerToClientStreamCipher(), frame)
        channel.writeAndFlush(encoded).addListener { future ->
            if (!future.isSuccess) {
                logger.error(future.cause()) {
                    "Unable to send replay frame ${frame.index} (${frame.prot}) at tick ${frame.tick}"
                }
            }
        }
    }

    private fun sendFastForwardFrames(frames: List<ReplayFrame>) {
        val channel =
            clientChannel.get()
                ?: return
        if (!channel.isActive) {
            return
        }
        val cipher = channel.getServerToClientStreamCipher()
        val allocator = channel.alloc()
        var groupBytes = 0
        val groupFrames = mutableListOf<ReplayFrame>()

        fun writeRawFrame(frame: ReplayFrame) {
            val encoded = ReplayPacketEncoder.encode(allocator, cipher, frame)
            channel.writeAndFlush(encoded).addListener { future ->
                if (!future.isSuccess) {
                    logger.error(future.cause()) {
                        "Unable to send replay fast-forward frame ${frame.index} (${frame.prot})"
                    }
                }
            }
        }

        fun flushGroup() {
            if (groupFrames.isEmpty()) {
                return
            }
            val start = packetGroupStartProt
            if (start == null) {
                groupFrames.forEach { frame ->
                    val encoded = ReplayPacketEncoder.encode(allocator, cipher, frame)
                    channel.write(encoded).addListener { future ->
                        if (!future.isSuccess) {
                            logger.error(future.cause()) { "Unable to send replay fast-forward packet" }
                        }
                    }
                }
                channel.flush()
            } else {
                channel.write(ReplayPacketEncoder.encode(allocator, cipher, start, packetGroupStartPayload(groupBytes)))
                groupFrames.dropLast(1).forEach { frame ->
                    channel.write(ReplayPacketEncoder.encode(allocator, cipher, frame))
                }
                channel
                    .writeAndFlush(ReplayPacketEncoder.encode(allocator, cipher, groupFrames.last()))
                    .addListener { future ->
                        if (!future.isSuccess) {
                            logger.error(future.cause()) {
                                "Unable to send replay fast-forward packet group of $groupBytes bytes"
                            }
                        }
                    }
            }
            groupFrames.clear()
            groupBytes = 0
        }

        for (frame in frames) {
            if (!channel.isActive) {
                break
            }
            if (frame.prot.isReplayPacketGroupMarker()) {
                continue
            }
            val size = frame.replayWireSize()
            if (frame.index == initialRebuildNormalFrameIndex) {
                flushGroup()
                writeRawFrame(frame)
                continue
            }
            if (size > MAX_PACKET_GROUP_BYTES) {
                flushGroup()
                writeRawFrame(frame)
                continue
            }
            if (groupBytes + size > MAX_PACKET_GROUP_BYTES) {
                flushGroup()
            }
            groupFrames += frame
            groupBytes += size
        }
        flushGroup()
    }

    private fun packetGroupStartPayload(length: Int): ByteArray {
        require(length in 0..Short.MAX_VALUE) {
            "Packet group length out of range: $length"
        }
        return byteArrayOf((length ushr 8).toByte(), length.toByte())
    }

    private fun forceReconnectForRebuild(
        targetTick: Int,
        resumeAfter: Boolean,
    ) {
        synchronized(rebuildLock) {
            pendingReconnectRebuild =
                PendingReconnectRebuild(
                    targetTick = targetTick,
                    resumeAfter = resumeAfter,
                    response = ReconnectResponse.LOGIN_OK,
                )
        }
        rebuildInProgress.set(true)
        controller.beginSeeking()
        cancelPausedKeepalive()
        clientChannel.getAndSet(null)?.takeIf { it.isActive }?.close()
    }

    private fun registerClientCloseListener(channel: Channel) {
        channel.closeFuture().addListener {
            detachClientChannel(channel)
        }
    }

    private fun detachClientChannel(channel: Channel) {
        if (!clientChannel.compareAndSet(channel, null)) {
            return
        }
        synchronized(rebuildLock) {
            pendingReconnectRebuild = null
            pendingReconnectAttach = null
        }
        rebuildInProgress.set(false)
        clientLaunchStarted.set(false)
        clientDisconnected.set(true)
        controller.pause()
        cancelPausedKeepalive()
    }

    private fun refreshPausedKeepalive() {
        val channel = clientChannel.get()
        val shouldRun =
            channel != null &&
                channel.isActive &&
                controller.snapshot().state == ReplayPlaybackState.PAUSED
        synchronized(keepaliveLock) {
            if (!shouldRun) {
                cancelPausedKeepaliveLocked()
            } else if (pausedKeepaliveTask == null) {
                schedulePausedKeepaliveLocked()
            }
        }
    }

    private fun sendPausedKeepalive() {
        synchronized(keepaliveLock) {
            pausedKeepaliveTask = null
        }
        val channel = clientChannel.get()
        if (channel == null || !channel.isActive || controller.snapshot().state != ReplayPlaybackState.PAUSED) {
            refreshPausedKeepalive()
            return
        }
        val prot = sendPingProt
        if (prot == null) {
            logger.warn { "Unable to send replay pause keepalive: SEND_PING prot is unavailable" }
            return
        }
        val payloadSize = prot.size
        if (payloadSize < 0) {
            logger.warn { "Unable to send replay pause keepalive: SEND_PING has unexpected variable size $payloadSize" }
            return
        }
        val encoded =
            ReplayPacketEncoder.encode(
                channel.alloc(),
                channel.getServerToClientStreamCipher(),
                prot,
                ByteArray(payloadSize),
            )
        channel.writeAndFlush(encoded).addListener { future ->
            if (!future.isSuccess) {
                logger.error(future.cause()) {
                    "Unable to send replay pause keepalive"
                }
            }
        }
        refreshPausedKeepalive()
    }

    private fun cancelPausedKeepalive() {
        synchronized(keepaliveLock) {
            cancelPausedKeepaliveLocked()
        }
    }

    private fun cancelPausedKeepaliveLocked() {
        pausedKeepaliveTask?.cancel()
        pausedKeepaliveTask = null
    }

    private fun schedulePausedKeepaliveLocked() {
        pausedKeepaliveTask =
            scheduler.schedule(PAUSED_KEEPALIVE_INTERVAL_MILLIS) {
                sendPausedKeepalive()
            }
    }

    private enum class ReconnectResponse {
        LOGIN_OK,
    }

    private data class PendingReconnectRebuild(
        val targetTick: Int,
        val resumeAfter: Boolean,
        val response: ReconnectResponse,
    )

    private data class PendingReconnectAttach(
        val resumeAfter: Boolean,
    )

    private companion object {
        private val logger = InlineLogger()
        private const val PAUSED_KEEPALIVE_INTERVAL_MILLIS: Long = 5_000
        private const val MAX_PACKET_GROUP_BYTES: Int = 30_000

    }
}
