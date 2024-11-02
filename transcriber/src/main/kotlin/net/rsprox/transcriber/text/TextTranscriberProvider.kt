package net.rsprox.transcriber.text

import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.indexing.BinaryIndex
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.shared.symbols.SymbolDictionaryProvider
import net.rsprox.transcriber.TranscriberPlugin
import net.rsprox.transcriber.TranscriberProvider
import net.rsprox.transcriber.TranscriberRunner
import net.rsprox.transcriber.state.SessionState

public class TextTranscriberProvider : TranscriberProvider {
    override fun provide(
        container: TextMessageConsumerContainer,
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        filters: PropertyFilterSetStore,
        settings: SettingSetStore,
        binaryIndex: BinaryIndex,
        state: SessionState,
    ): TranscriberRunner {
        val dictionary = SymbolDictionaryProvider.get()
        val formatter =
            OmitFilteredPropertyTreeFormatter(
                PropertyFormatterCollection.default(
                    dictionary,
                    settings,
                ),
            )
        val monitoredContainer = MonitoredMessageConsumerContainer(container, monitor)
        return TranscriberPlugin(
            TextTranscriber(
                cacheProvider,
                state,
                monitoredContainer,
                formatter,
                filters,
                settings,
            ),
        )
    }
}
