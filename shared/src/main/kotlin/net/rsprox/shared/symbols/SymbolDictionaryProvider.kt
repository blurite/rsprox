package net.rsprox.shared.symbols

import net.rsprox.shared.property.SymbolDictionary
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

public object SymbolDictionaryProvider {
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
                    WatchServiceSymbolDictionary(typeEntries)
                } else {
                    SymbolDictionary.EMPTY_SYMBOL_DICTIONARY
                }
            symbolDictionary.start()
        }
    }

    public fun stop() {
        if (::symbolDictionary.isInitialized) {
            symbolDictionary.stop()
        }
    }

    public fun get(): SymbolDictionary {
        loadSymbolDictionary()
        return symbolDictionary
    }
}
