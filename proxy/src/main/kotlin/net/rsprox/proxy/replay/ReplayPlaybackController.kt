package net.rsprox.proxy.replay

import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.Prot
import net.rsprox.shared.StreamDirection
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.max

public enum class ReplayPlaybackState {
    STOPPED,
    PLAYING,
    SEEKING,
    WAITING_FOR_MAP_BUILD_COMPLETE,
    PAUSED,
    FINISHED,
}

public data class ReplayPlaybackSnapshot(
    public val state: ReplayPlaybackState,
    public val currentTick: Int,
    public val totalTicks: Int,
    public val nextFrameIndex: Int,
    public val totalFrames: Int,
    public val speed: Double,
)

public fun interface ReplayScheduler {
    public fun schedule(
        delayMillis: Long,
        task: () -> Unit,
    ): ReplayScheduledTask
}

public fun interface ReplayScheduledTask {
    public fun cancel()
}

public class ExecutorReplayScheduler(
    private val executor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "rsprox-replay").apply {
                isDaemon = true
            }
        },
) : ReplayScheduler,
    AutoCloseable {
    override fun schedule(
        delayMillis: Long,
        task: () -> Unit,
    ): ReplayScheduledTask {
        val future: ScheduledFuture<*> =
            executor.schedule(
                task,
                delayMillis,
                TimeUnit.MILLISECONDS,
            )
        return ReplayScheduledTask { future.cancel(false) }
    }

    override fun close() {
        executor.shutdownNow()
    }
}

public class ReplayPlaybackController(
    private val timeline: ReplayTimeline,
    private val scheduler: ReplayScheduler,
    private val onFinished: () -> Unit = {},
    private val fastForwardSink: ((List<ReplayFrame>) -> Unit)? = null,
    private val skipRedundantFastForwardFrames: Boolean = true,
    private val fastForwardBatchSize: Int = Int.MAX_VALUE,
    private val waitForIntermediateMapBuilds: Boolean = true,
    private val onReconnect: ((ReplayFrame) -> Unit)? = null,
    private val sink: (ReplayFrame) -> Unit,
) {
    private var state: ReplayPlaybackState = ReplayPlaybackState.STOPPED
    private var nextFrameIndex: Int = 0
    private var activeWorld: Int = -1
    private var speed: Double = 1.0
    private var scheduledTask: ReplayScheduledTask? = null
    private var pendingMapBuildCompleteTick: Int? = null
    private var resumeAfterMapBuildComplete: Boolean = false
    private var pendingFastForward: PendingFastForward? = null
    private var pendingReconnect: PendingReconnect? = null

    @Synchronized
    public fun snapshot(): ReplayPlaybackSnapshot {
        return ReplayPlaybackSnapshot(
            state = state,
            currentTick = currentTick(),
            totalTicks = timeline.totalTicks,
            nextFrameIndex = nextFrameIndex,
            totalFrames = timeline.frames.size,
            speed = speed,
        )
    }

    @Synchronized
    public fun play() {
        if (pendingReconnect != null) return
        if (state == ReplayPlaybackState.PLAYING || state == ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE) {
            return
        }
        if (state == ReplayPlaybackState.FINISHED) {
            nextFrameIndex = 0
            pendingMapBuildCompleteTick = null
        }
        if (shouldWaitForMapBuildCompleteLocked()) {
            waitForMapBuildCompleteLocked(resumeAfter = true)
            return
        }
        state = ReplayPlaybackState.PLAYING
        scheduleNextLocked()
    }

    @Synchronized
    public fun pause() {
        pendingReconnect?.let {
            pendingReconnect = it.copy(resumeAfter = false)
            pendingFastForward = pendingFastForward?.copy(resumeAfter = false)
            return
        }
        if (state != ReplayPlaybackState.PLAYING && state != ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE) {
            return
        }
        cancelScheduledLocked()
        state = ReplayPlaybackState.PAUSED
    }

    @Synchronized
    public fun resume() {
        play()
    }

    @Synchronized
    public fun stop() {
        cancelScheduledLocked()
        nextFrameIndex = 0
        pendingMapBuildCompleteTick = null
        resumeAfterMapBuildComplete = false
        pendingFastForward = null
        pendingReconnect = null
        state = ReplayPlaybackState.STOPPED
    }

    @Synchronized
    public fun beginSeeking() {
        cancelScheduledLocked()
        pendingMapBuildCompleteTick = null
        resumeAfterMapBuildComplete = false
        pendingFastForward = null
        pendingReconnect = null
        if (state != ReplayPlaybackState.FINISHED) {
            state = ReplayPlaybackState.SEEKING
        }
    }

    @Synchronized
    public fun seekToTick(tick: Int) {
        cancelScheduledLocked()
        nextFrameIndex = timeline.firstFrameIndexAtOrAfterTick(tick)
        pendingMapBuildCompleteTick = null
        resumeAfterMapBuildComplete = false
        pendingFastForward = null
        pendingReconnect = null
        if (state == ReplayPlaybackState.FINISHED ||
            state == ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE ||
            state == ReplayPlaybackState.SEEKING
        ) {
            state = ReplayPlaybackState.PAUSED
        }
        if (state == ReplayPlaybackState.PLAYING) {
            scheduleNextLocked()
        }
    }

    @Synchronized
    public fun fastForwardToTick(
        tick: Int,
        resumeAfter: Boolean,
    ): Int {
        check(pendingReconnect == null) { "Replay is waiting for its reconnect" }
        cancelScheduledLocked()
        pendingMapBuildCompleteTick = null
        resumeAfterMapBuildComplete = false
        val targetFrameIndex = timeline.firstFrameIndexAtOrAfterTick(tick)
        pendingFastForward =
            PendingFastForward(
                tick,
                resumeAfter,
                targetFrameIndex,
                latestMapRebuildIndexBeforeLocked(targetFrameIndex),
            )
        return continueFastForwardLocked()
    }

    private fun continueFastForwardLocked(): Int {
        val fastForward = pendingFastForward ?: return currentTick()
        val targetFrameIndex = fastForward.targetFrameIndex
        val latestMapRebuildIndex = fastForward.finalRebuildFrameIndex
        val frames = mutableListOf<ReplayFrame>()
        while (nextFrameIndex < targetFrameIndex) {
            if (shouldWaitForMapBuildCompleteLocked()) {
                flushFastForwardFramesLocked(frames)
                waitForMapBuildCompleteLocked(resumeAfter = false)
                return currentTick()
            }
            val frame = timeline.frames[nextFrameIndex]
            if (onReconnect != null &&
                frame.prot.isReplayReconnect() &&
                frame.direction == StreamDirection.SERVER_TO_CLIENT
            ) {
                flushFastForwardFramesLocked(frames)
                suspendForReconnectLocked(frame, fastForward.resumeAfter)
                return currentTick()
            }
            if (frame.prot.isActiveWorldV1()) {
                val buf = Unpooled.wrappedBuffer(frame.payload).toJagByteBuf()
                val type = buf.g1()
                val index = buf.g2()
                this.activeWorld = if (type == 0) -1 else index
            } else if (frame.prot.isActiveWorldV2()) {
                val buf = Unpooled.wrappedBuffer(frame.payload).toJagByteBuf()
                val index = buf.g2s()
                this.activeWorld = if (index <= 0) -1 else index
            }
            if (skipRedundantFastForwardFrames &&
                shouldSkipRedundantFastForwardFrameLocked(
                    nextFrameIndex,
                    latestMapRebuildIndex,
                    fastForward.targetTick,
                )
            ) {
                nextFrameIndex++
                continue
            }
            frames += frame
            markMapBuildBarrierIfNeededLocked(frame)
            nextFrameIndex++
            if (frames.size >= fastForwardBatchSize && nextFrameIndex < targetFrameIndex) {
                flushFastForwardFramesLocked(frames)
                state = ReplayPlaybackState.SEEKING
                scheduledTask = scheduler.schedule(0) { continueFastForward() }
                return currentTick()
            }
        }
        flushFastForwardFramesLocked(frames)
        if (shouldWaitForMapBuildCompleteLocked()) {
            waitForMapBuildCompleteLocked(resumeAfter = false)
        } else if (nextFrameIndex >= timeline.frames.size) {
            pendingFastForward = null
            finishLocked()
        } else {
            pendingFastForward = null
            state = ReplayPlaybackState.PAUSED
        }
        if (fastForward.resumeAfter &&
            state != ReplayPlaybackState.FINISHED &&
            state != ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE
        ) {
            state = ReplayPlaybackState.PLAYING
            scheduleNextLocked()
        }
        return currentTick()
    }

    private fun flushFastForwardFramesLocked(frames: MutableList<ReplayFrame>) {
        if (frames.isEmpty()) {
            return
        }
        val snapshot = frames.toList()
        if (fastForwardSink != null) {
            fastForwardSink.invoke(snapshot)
        } else {
            snapshot.forEach(sink)
        }
        frames.clear()
    }

    @Synchronized
    private fun continueFastForward() {
        if (state == ReplayPlaybackState.SEEKING) continueFastForwardLocked()
    }

    @Synchronized
    public fun prepareReconnectBootstrapFromCurrent(extract: (ReplayFrame) -> ReplayReconnectBootstrap?): ByteArray? {
        while (timeline.frames.getOrNull(nextFrameIndex)?.direction == StreamDirection.CLIENT_TO_SERVER) {
            nextFrameIndex++
        }
        val frame = timeline.frames.getOrNull(nextFrameIndex) ?: return null
        val bootstrap = extract(frame) ?: return null
        cancelScheduledLocked()
        pendingMapBuildCompleteTick = null
        resumeAfterMapBuildComplete = false
        pendingFastForward = null
        state = ReplayPlaybackState.PAUSED
        if (bootstrap.consumeFrame) {
            nextFrameIndex++
        }
        return bootstrap.payload
    }

    @Synchronized
    public fun stepForwardTick(): Int {
        if (pendingReconnect != null) return currentTick()
        cancelScheduledLocked()
        if (timeline.frames.isEmpty() || nextFrameIndex >= timeline.frames.size) {
            finishLocked()
            return currentTick()
        }
        val tick = timeline.frames[nextFrameIndex].tick
        var emitted = 0
        while (nextFrameIndex < timeline.frames.size && timeline.frames[nextFrameIndex].tick == tick) {
            val frame = timeline.frames[nextFrameIndex]
            if (suspendForReconnectLocked(frame, resumeAfter = false, stepTick = tick)) return currentTick()
            sink(frame)
            markMapBuildBarrierIfNeededLocked(frame)
            nextFrameIndex++
            emitted++
        }
        if (shouldWaitForMapBuildCompleteLocked()) {
            waitForMapBuildCompleteLocked(resumeAfter = false)
        } else if (nextFrameIndex >= timeline.frames.size) {
            finishLocked()
        } else {
            state = ReplayPlaybackState.PAUSED
        }
        return currentTick()
    }

    @Synchronized
    public fun stepBackTick(): Int {
        val target = max(0, currentTick() - 1)
        seekToTick(target)
        state = ReplayPlaybackState.PAUSED
        return currentTick()
    }

    @Synchronized
    public fun setSpeed(multiplier: Double) {
        require(multiplier > 0.0) {
            "Replay speed multiplier must be positive: $multiplier"
        }
        speed = multiplier
        if (state == ReplayPlaybackState.PLAYING) {
            cancelScheduledLocked()
            scheduleNextLocked()
        }
    }

    @Synchronized
    public fun mapBuildComplete() {
        if (pendingReconnect != null) return
        if (pendingMapBuildCompleteTick == null) {
            return
        }
        pendingMapBuildCompleteTick = null
        if (pendingFastForward != null) {
            continueFastForwardLocked()
            return
        }
        val resume = resumeAfterMapBuildComplete
        resumeAfterMapBuildComplete = false
        if (state == ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE) {
            if (resume) {
                state = ReplayPlaybackState.PLAYING
                scheduleNextLocked()
            } else {
                state = ReplayPlaybackState.PAUSED
            }
        }
    }

    @Synchronized
    private fun scheduleNextLocked() {
        cancelScheduledLocked()
        if (shouldWaitForMapBuildCompleteLocked()) {
            waitForMapBuildCompleteLocked(resumeAfter = true)
            return
        }
        if (nextFrameIndex >= timeline.frames.size) {
            finishLocked()
            return
        }
        val frame = timeline.frames[nextFrameIndex]
        val delay = (frame.delayMillis / speed).toLong().coerceAtLeast(0)
        scheduledTask =
            scheduler.schedule(delay) {
                emitScheduledFrame()
            }
    }

    @Synchronized
    private fun emitScheduledFrame() {
        if (state != ReplayPlaybackState.PLAYING || nextFrameIndex >= timeline.frames.size) {
            return
        }
        val frame = timeline.frames[nextFrameIndex]
        if (suspendForReconnectLocked(frame, resumeAfter = true)) return
        sink(frame)
        markMapBuildBarrierIfNeededLocked(frame)
        nextFrameIndex++
        scheduleNextLocked()
    }

    private fun suspendForReconnectLocked(
        frame: ReplayFrame,
        resumeAfter: Boolean,
        stepTick: Int? = null,
    ): Boolean {
        val callback = onReconnect ?: return false
        if (frame.direction != StreamDirection.SERVER_TO_CLIENT || !frame.prot.isReplayReconnect()) return false
        cancelScheduledLocked()
        nextFrameIndex++
        pendingMapBuildCompleteTick = null
        resumeAfterMapBuildComplete = false
        pendingReconnect = PendingReconnect(resumeAfter, stepTick)
        state = ReplayPlaybackState.SEEKING
        callback(frame)
        return true
    }

    @Synchronized
    public fun resumeAfterReconnect() {
        val reconnect = checkNotNull(pendingReconnect) { "Replay has no pending reconnect" }
        pendingReconnect = null
        if (pendingFastForward != null) {
            continueFastForwardLocked()
        } else if (reconnect.stepTick != null &&
            timeline.frames.getOrNull(nextFrameIndex)?.tick == reconnect.stepTick
        ) {
            stepForwardTick()
        } else if (nextFrameIndex >= timeline.frames.size) {
            finishLocked()
        } else {
            state = ReplayPlaybackState.PAUSED
            if (reconnect.resumeAfter) play()
        }
    }

    private data class PendingReconnect(
        val resumeAfter: Boolean,
        val stepTick: Int?,
    )

    private fun currentTick(): Int {
        if (timeline.frames.isEmpty()) return 0
        if (nextFrameIndex >= timeline.frames.size) return timeline.totalTicks
        return timeline.frames[nextFrameIndex].tick
    }

    private fun cancelScheduledLocked() {
        scheduledTask?.cancel()
        scheduledTask = null
    }

    private fun finishLocked() {
        if (state != ReplayPlaybackState.FINISHED) {
            pendingFastForward = null
            state = ReplayPlaybackState.FINISHED
            onFinished()
        }
    }

    private fun shouldWaitForMapBuildCompleteLocked(): Boolean {
        val rebuildTick = pendingMapBuildCompleteTick ?: return false
        // A seek to the end must still finish its final scene before reporting FINISHED.
        val nextTick = timeline.frames.getOrNull(nextFrameIndex)?.tick ?: return pendingFastForward != null
        return nextTick > rebuildTick
    }

    private fun markMapBuildBarrierIfNeededLocked(frame: ReplayFrame) {
        val fastForward = pendingFastForward
        if (fastForward != null &&
            !waitForIntermediateMapBuilds &&
            nextFrameIndex != fastForward.finalRebuildFrameIndex
        ) {
            return
        }
        if (frame.direction != StreamDirection.SERVER_TO_CLIENT || !frame.prot.isReplayMapRebuild()) {
            return
        }
        pendingMapBuildCompleteTick = maxOf(pendingMapBuildCompleteTick ?: frame.tick, frame.tick)
    }

    private fun waitForMapBuildCompleteLocked(resumeAfter: Boolean) {
        resumeAfterMapBuildComplete = resumeAfter
        state = ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE
    }

    private data class PendingFastForward(
        val targetTick: Int,
        val resumeAfter: Boolean,
        val targetFrameIndex: Int,
        val finalRebuildFrameIndex: Int?,
    )

    private fun latestMapRebuildIndexBeforeLocked(targetFrameIndex: Int): Int? {
        for (index in targetFrameIndex - 1 downTo nextFrameIndex) {
            val frame = timeline.frames[index]
            if (frame.direction == StreamDirection.SERVER_TO_CLIENT && frame.prot.isReplayMapRebuild()) {
                return index
            }
        }
        return null
    }

    private fun shouldSkipRedundantFastForwardFrameLocked(
        frameIndex: Int,
        latestMapRebuildIndex: Int?,
        targetTick: Int,
    ): Boolean {
        val frame = timeline.frames[frameIndex]
        if (frame.direction != StreamDirection.SERVER_TO_CLIENT) {
            return false
        }
        if (frame.tick < targetTick && frame.prot.isReplaySynthSound()) {
            return true
        }
        val latestIndex = latestMapRebuildIndex ?: return false
        if (frameIndex >= latestIndex) {
            return false
        }
        if (frame.prot.isInitialRebuildNormal(frameIndex)) {
            return false
        }
        // The client has a tendency to glitch out when on worldentities, so avoid skipping during it.
        if (frame.prot.isReplayIncomingZoneUpdate()) {
            return activeWorld == -1
        }
        return frame.prot.isReplayMapRebuild()
    }

    private fun Prot.isInitialRebuildNormal(frameIndex: Int): Boolean {
        if (!isReplayRebuildNormal()) {
            return false
        }
        return timeline.frames
            .asSequence()
            .take(frameIndex)
            .filter { it.direction == StreamDirection.SERVER_TO_CLIENT }
            .none { it.prot.isReplayRebuildNormal() }
    }
}
