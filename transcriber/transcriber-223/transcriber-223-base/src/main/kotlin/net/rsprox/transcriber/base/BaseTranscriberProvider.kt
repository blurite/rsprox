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
import net.rsprox.transcriber.base.symbols.StaticSymbolDictionary
import net.rsprox.transcriber.base.symbols.SymbolProperties
import net.rsprox.transcriber.state.StateTracker
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

public class BaseTranscriberProvider : TranscriberProvider {
    override fun provide(
        container: BaseMessageConsumerContainer,
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        filters: PropertyFilterSetStore,
    ): TranscriberRunner {
        loadSymbolDictionary()
        val dictionary = symbolDictionary
        val stateTracker = StateTracker()
        val formatter =
            OmitFilteredPropertyTreeFormatter(
                PropertyFormatterCollection.default(
                    dictionary,
                ),
            )
        val monitoredContainer = MonitoredMessageConsumerContainer(container, monitor)
        return TranscriberPlugin(
            BaseTranscriber(
                cacheProvider,
                monitor,
                stateTracker,
                monitoredContainer,
                formatter,
                filters,
            ),
        )
    }

    private companion object {
        private val DEFAULT_PROPERTIES_CHARSET: Charset = Charsets.ISO_8859_1
        private val CONFIGURATION_PATH: Path = Path(System.getProperty("user.home"), ".rsprox")
        private val SYMBOLS_PATH: Path = CONFIGURATION_PATH.resolve("symbols.properties")
        private lateinit var symbolDictionary: SymbolDictionary

        private fun loadSymbolDictionary() {
            if (!::symbolDictionary.isInitialized) {
                symbolDictionary =
                    if (SYMBOLS_PATH.exists()) {
                        val properties = Properties()
                        properties.load(SYMBOLS_PATH.readText().byteInputStream(DEFAULT_PROPERTIES_CHARSET))
                        val symbolProperties = SymbolProperties.load(properties)
                        val typeEntries = symbolProperties.buildSymbolTypeEntries()
                        StaticSymbolDictionary(typeEntries)
                    } else {
                        SymbolDictionary.EMPTY_SYMBOL_DICTIONARY
                    }
            }
        }
    }
}
