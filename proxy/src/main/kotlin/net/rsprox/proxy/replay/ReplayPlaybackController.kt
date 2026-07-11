package net.rsprox.proxy.replay

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
    private val sink: (ReplayFrame) -> Unit,
) {
    private var state: ReplayPlaybackState = ReplayPlaybackState.STOPPED
    private var nextFrameIndex: Int = 0
    private var speed: Double = 1.0
    private var scheduledTask: ReplayScheduledTask? = null
    private var pendingMapBuildCompleteTick: Int? = null
    private var resumeAfterMapBuildComplete: Boolean = false
    private var pendingFastForward: PendingFastForward? = null

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
        state = ReplayPlaybackState.STOPPED
    }

    @Synchronized
    public fun beginSeeking() {
        cancelScheduledLocked()
        pendingMapBuildCompleteTick = null
        resumeAfterMapBuildComplete = false
        pendingFastForward = null
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
        cancelScheduledLocked()
        pendingMapBuildCompleteTick = null
        resumeAfterMapBuildComplete = false
        pendingFastForward = PendingFastForward(tick, resumeAfter)
        return continueFastForwardLocked()
    }

    private fun continueFastForwardLocked(): Int {
        val fastForward = pendingFastForward ?: return currentTick()
        val targetFrameIndex = timeline.firstFrameIndexAtOrAfterTick(fastForward.targetTick)
        val latestMapRebuildIndex = latestMapRebuildIndexBeforeLocked(targetFrameIndex)
        val frames = mutableListOf<ReplayFrame>()
        while (nextFrameIndex < targetFrameIndex) {
            if (shouldWaitForMapBuildCompleteLocked()) {
                flushFastForwardFramesLocked(frames)
                waitForMapBuildCompleteLocked(resumeAfter = false)
                return currentTick()
            }
            val frame = timeline.frames[nextFrameIndex]
            if (shouldSkipRedundantFastForwardFrameLocked(
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
        cancelScheduledLocked()
        if (timeline.frames.isEmpty() || nextFrameIndex >= timeline.frames.size) {
            finishLocked()
            return currentTick()
        }
        val tick = timeline.frames[nextFrameIndex].tick
        var emitted = 0
        while (nextFrameIndex < timeline.frames.size && timeline.frames[nextFrameIndex].tick == tick) {
            val frame = timeline.frames[nextFrameIndex]
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
        sink(frame)
        markMapBuildBarrierIfNeededLocked(frame)
        nextFrameIndex++
        scheduleNextLocked()
    }

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
        val nextTick = timeline.frames.getOrNull(nextFrameIndex)?.tick ?: return false
        return nextTick > rebuildTick
    }

    private fun markMapBuildBarrierIfNeededLocked(frame: ReplayFrame) {
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
        return frame.prot.isReplayMapRebuild() || frame.prot.isReplayIncomingZoneUpdate()
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
