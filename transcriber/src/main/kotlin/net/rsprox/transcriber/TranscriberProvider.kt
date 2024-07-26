package net.rsprox.transcriber

import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor

public fun interface TranscriberProvider {
    public fun provide(
        container: BaseMessageConsumerContainer,
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
    ): TranscriberRunner
}
