package net.rsprox.proxy.rs3

import com.github.michaelbull.logging.InlineLogger

/** Progress is local to this stage, not an estimate of the complete launch duration. */
public data class Rs3LaunchProgress(
    public val stage: String,
    public val completed: Long = 0,
    public val total: Long = 0,
) {
    public val percent: Int?
        get() = if (total > 0) (completed.toDouble() / total * 100).toInt().coerceIn(0, 100) else null
}

internal class Rs3LaunchTracker(
    private val launchId: Int,
    private val listener: (Rs3LaunchProgress) -> Unit,
) {
    private var stage: String? = null
    private var started = System.nanoTime()
    private var lastPercent: Int? = null

    @Synchronized
    fun update(progress: Rs3LaunchProgress) {
        if (stage != progress.stage) {
            logDuration()
            stage = progress.stage
            started = System.nanoTime()
            lastPercent = progress.percent
            logger.info { "RS3 launch $launchId: ${progress.stage}" }
            listener(progress)
        } else if (lastPercent != progress.percent) {
            lastPercent = progress.percent
            listener(progress)
        }
    }

    @Synchronized
    fun complete() {
        logDuration()
        stage = null
    }

    private fun logDuration() {
        val name = stage ?: return
        val elapsed = (System.nanoTime() - started) / 1_000_000
        logger.info { "RS3 launch $launchId: $name finished after ${elapsed} ms" }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
