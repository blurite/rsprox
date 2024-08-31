package net.rsprox.transcriber

import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.indexing.BinaryIndex
import net.rsprox.shared.settings.SettingSetStore

public fun interface TranscriberProvider {
    public fun provide(
        container: BaseMessageConsumerContainer,
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        filters: PropertyFilterSetStore,
        settings: SettingSetStore,
        binaryIndex: BinaryIndex,
    ): TranscriberRunner
}
