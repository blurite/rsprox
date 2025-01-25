package net.rsprox.proxy.worlds

import net.rsprox.proxy.target.ProxyTargetConfig
import java.net.URL
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

public class DynamicWorldListProvider(
    private val proxyTargetConfig: ProxyTargetConfig,
    private val originalWorldListUrl: URL,
    private val cacheDurationSeconds: Int = 5,
) : WorldListProvider {
    private var cached: WorldList = WorldList(proxyTargetConfig, originalWorldListUrl)
    private var lastUpdate: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

    override fun get(): WorldList {
        if (lastUpdate.elapsedNow() > cacheDurationSeconds.seconds) {
            lastUpdate = TimeSource.Monotonic.markNow()
            cached = WorldList(proxyTargetConfig, originalWorldListUrl)
        }
        return cached
    }
}
