package net.rsprox.shared.property

import net.rsprox.shared.ScriptVarType

public interface SymbolDictionary {
    public fun getScriptVarTypeName(
        id: Int,
        type: ScriptVarType,
    ): String?

    public fun getVarpName(id: Int): String?

    public fun getVarbitName(id: Int): String?

    public fun getScriptName(id: Int): String?

    public fun start()

    public fun stop()

    public companion object {
        public val EMPTY_SYMBOL_DICTIONARY: SymbolDictionary =
            object : SymbolDictionary {
                override fun getScriptVarTypeName(
                    id: Int,
                    type: ScriptVarType,
                ): String? {
                    return null
                }

                override fun getVarpName(id: Int): String? {
                    return null
                }

                override fun getVarbitName(id: Int): String? {
                    return null
                }

                override fun getScriptName(id: Int): String? {
                    return null
                }

                override fun start() {
                }

                override fun stop() {
                }
            }
    }
}
