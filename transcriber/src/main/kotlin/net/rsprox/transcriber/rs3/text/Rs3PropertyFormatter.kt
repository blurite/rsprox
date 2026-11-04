package net.rsprox.transcriber.rs3.text

import net.rsprox.cache.api.rs3.Rs3Gameval
import net.rsprox.cache.api.type.ClientScriptDefinitionProvider
import net.rsprox.shared.property.NopSymbolDictionary
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSetStore

/** Shared by live GUI previews and text output, using symbols supplied by the caller. */
public object Rs3PropertyFormatter {
    public fun create(
        settings: SettingSetStore,
        symbols: SymbolDictionary,
        clientScripts: ClientScriptDefinitionProvider = ClientScriptDefinitionProvider.EMPTY,
    ): OmitFilteredPropertyTreeFormatter {
        val dictionary =
            object : SymbolDictionary by symbols {
                override fun getRs3GamevalName(id: Int, type: Rs3Gameval): String? =
                    symbols.getRs3GamevalName(id, type)

                override fun getScriptName(id: Int): String? {
                    if (!settings.getActive()[Setting.INFER_CLIENTSCRIPT_NAMES]) return null
                    return clientScripts.getClientScriptDefinition(id)?.name
                }
            }
        return OmitFilteredPropertyTreeFormatter(
            PropertyFormatterCollection.withDictionary(settings.getActive()) {
                if (settings.getActive()[Setting.DISABLE_DICTIONARY_NAMES]) {
                    NopSymbolDictionary
                } else {
                    dictionary.also { it.start() }
                }
            },
        )
    }
}
