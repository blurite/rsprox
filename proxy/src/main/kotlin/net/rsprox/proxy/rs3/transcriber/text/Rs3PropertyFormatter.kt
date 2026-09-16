package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.proxy.rs3.gameval.Rs3GamevalLookup
import net.rsprox.shared.property.NopSymbolDictionary
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSetStore

/** Shared by live GUI previews and text output so property types render identically. */
public object Rs3PropertyFormatter {
    public fun create(settings: SettingSetStore): OmitFilteredPropertyTreeFormatter {
        return OmitFilteredPropertyTreeFormatter(
            PropertyFormatterCollection.withDictionary(settings.getActive()) {
                if (settings.getActive()[Setting.DISABLE_DICTIONARY_NAMES]) {
                    NopSymbolDictionary
                } else {
                    Rs3GamevalLookup.also { it.start() }
                }
            },
        )
    }
}
