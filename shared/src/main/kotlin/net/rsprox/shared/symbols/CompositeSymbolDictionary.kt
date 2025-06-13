package net.rsprox.shared.symbols

import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolDictionary

public class CompositeSymbolDictionary(
    private val dictionaries: List<SymbolDictionary>,
) : SymbolDictionary {
    public constructor(vararg dictionaries: SymbolDictionary) : this(dictionaries.toList())

    override fun getScriptVarTypeName(
        id: Int,
        type: ScriptVarType,
    ): String? {
        return dictionaries.firstNotNullOfOrNull { dictionary ->
            dictionary.getScriptVarTypeName(id, type)
        }
    }

    override fun getVarpName(id: Int): String? {
        return dictionaries.firstNotNullOfOrNull { dictionary ->
            dictionary.getVarpName(id)
        }
    }

    override fun getVarbitName(id: Int): String? {
        return dictionaries.firstNotNullOfOrNull { dictionary ->
            dictionary.getVarbitName(id)
        }
    }

    override fun getScriptName(id: Int): String? {
        return dictionaries.firstNotNullOfOrNull { dictionary ->
            dictionary.getScriptName(id)
        }
    }

    override fun start() {
        for (dictionary in dictionaries) {
            dictionary.start()
        }
    }

    override fun stop() {
        for (dictionary in dictionaries) {
            dictionary.stop()
        }
    }
}
