package net.rsprox.proxy.rs3.replay

import net.rsprox.proxy.replay.ReplayPlaybackSession
import net.rsprox.proxy.replay.ReplayPlaybackSnapshot
import net.rsprox.proxy.replay.ReplayPlaybackState
import net.rsprox.proxy.replay.ReplayTimeline
import java.util.concurrent.atomic.AtomicBoolean

public class Rs3ReplaySession(
    public val recording: Rs3ReplayRecording,
) : ReplayPlaybackSession {
    override val timeline: ReplayTimeline get() = recording.game

    @Volatile private var client: Rs3ReplayClient? = null

    @Volatile private var speed = 1.0

    @Volatile override var failureMessage: String? = null
        private set
    private val launched = AtomicBoolean()
    private val closed = AtomicBoolean()
    public val isClosed: Boolean get() = closed.get()

    @Synchronized internal fun reserveLaunch() {
        if (client?.isClosed == true) launched.set(false)
        check(!closed.get() && launched.compareAndSet(false, true)) { "Replay already launched or closed" }
        failureMessage = null
    }

    @Synchronized internal fun attach(prepared: Rs3ReplayClient) {
        if (closed.get()) {
            prepared.close()
            error("Replay closed during launch")
        }
        client = prepared
        prepared.server.setSpeed(speed)
    }

    internal fun failed(error: Throwable) {
        failureMessage = error.cause?.message ?: error.message ?: error.javaClass.simpleName
        releaseClient()
    }

    override fun hasActiveClient(): Boolean = client?.let { !it.isClosed && it.server.hasGameClient } == true

    override fun hasClientLaunchStarted(): Boolean = launched.get() && client?.isClosed != true

    override fun hasClientDisconnected(): Boolean = client?.isClosed == true

    override fun isRebuildInProgress(): Boolean = client?.server?.isRebuilding == true

    override fun snapshot(): ReplayPlaybackSnapshot =
        client?.server?.playbackSnapshot
            ?: ReplayPlaybackSnapshot(ReplayPlaybackState.STOPPED, 0, timeline.totalTicks, 0, timeline.size, speed)

    override fun play() {
        if (hasActiveClient()) client?.server?.play()
    }

    override fun pause() {
        client?.server?.pause()
    }

    override fun stop() {
        client?.server?.stopPlayback()
    }

    @Synchronized private fun releaseClient() {
        client?.close()
        client = null
        launched.set(false)
    }

    override fun setSpeed(multiplier: Double) {
        require(multiplier > 0 && multiplier.isFinite())
        speed = multiplier
        client?.server?.setSpeed(multiplier)
    }

    override fun seekToTick(tick: Int) {
        client?.server?.seekToTick(tick)
    }

    override fun stepForwardTick(): Int {
        client?.server?.stepForward()
        return snapshot().currentTick
    }

    override fun stepBackTick(): Int {
        client?.server?.stepBack()
        return snapshot().currentTick
    }

    override fun close() {
        closed.set(true)
        releaseClient()
    }
}
