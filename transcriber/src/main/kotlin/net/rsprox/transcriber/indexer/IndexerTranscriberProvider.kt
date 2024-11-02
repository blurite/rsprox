package net.rsprox.transcriber.indexer

import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.indexing.BinaryIndex
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.TranscriberPlugin
import net.rsprox.transcriber.TranscriberProvider
import net.rsprox.transcriber.TranscriberRunner
import net.rsprox.transcriber.state.StateTracker
import net.rsprox.transcriber.text.TextMessageConsumerContainer

public class IndexerTranscriberProvider : TranscriberProvider {
    override fun provide(
        container: TextMessageConsumerContainer,
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        filters: PropertyFilterSetStore,
        settings: SettingSetStore,
        binaryIndex: BinaryIndex,
    ): TranscriberRunner {
        val stateTracker = StateTracker(settings)
        return TranscriberPlugin(
            IndexerTranscriber(
                cacheProvider,
                monitor,
                stateTracker,
                binaryIndex,
            ),
        )
    }
}
