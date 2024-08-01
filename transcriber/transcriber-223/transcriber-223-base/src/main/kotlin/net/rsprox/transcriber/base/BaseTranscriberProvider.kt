package net.rsprox.transcriber.base

import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.transcriber.BaseMessageConsumerContainer
import net.rsprox.transcriber.TranscriberPlugin
import net.rsprox.transcriber.TranscriberProvider
import net.rsprox.transcriber.TranscriberRunner
import net.rsprox.transcriber.state.StateTracker

public class BaseTranscriberProvider : TranscriberProvider {
    override fun provide(
        container: BaseMessageConsumerContainer,
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        filters: PropertyFilterSetStore,
    ): TranscriberRunner {
        val stateTracker = StateTracker()
        val formatter =
            OmitFilteredPropertyTreeFormatter(
                PropertyFormatterCollection.default(
                    SymbolDictionary.EMPTY_SYMBOL_DICTIONARY,
                ),
            )
        return TranscriberPlugin(
            BaseTranscriber(
                cacheProvider,
                monitor,
                stateTracker,
                container,
                formatter,
                filters,
            ),
        )
    }
}
