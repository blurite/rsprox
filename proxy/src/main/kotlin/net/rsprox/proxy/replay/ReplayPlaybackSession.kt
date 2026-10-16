package net.rsprox.proxy.replay

/** Playback operations exposed to the shared replay panel. */
public interface ReplayPlaybackSession : AutoCloseable {
    public val timeline: ReplayTimeline
    public val failureMessage: String? get() = null

    public fun snapshot(): ReplayPlaybackSnapshot

    public fun hasActiveClient(): Boolean

    public fun hasClientLaunchStarted(): Boolean

    public fun hasClientDisconnected(): Boolean

    public fun isRebuildInProgress(): Boolean

    public fun play()

    public fun pause()

    public fun stop()

    public fun setSpeed(multiplier: Double)

    public fun seekToTick(tick: Int)

    public fun stepForwardTick(): Int

    public fun stepBackTick(): Int
}
