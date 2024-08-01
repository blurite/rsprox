package net.rsprox.shared.symbols

import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.property.SymbolType

public class StaticSymbolDictionary(
    typeEntries: Map<SymbolType, SymbolTypeEntry>,
) : SymbolDictionary {
    private val symbols =
        typeEntries.entries.associate { (k, v) ->
            k to v.read()
        }

    override fun getScriptVarTypeName(
        id: Int,
        type: ScriptVarType,
    ): String? {
        val symbolMap = symbols[SymbolType.ScriptVarTypeSymbol(type)] ?: return null
        return symbolMap[id]
    }

    override fun getVarpName(id: Int): String? {
        val symbolMap = symbols[SymbolType.VarpSymbol] ?: return null
        return symbolMap[id]
    }

    override fun getVarbitName(id: Int): String? {
        val symbolMap = symbols[SymbolType.VarbitSymbol] ?: return null
        return symbolMap[id]
    }

    override fun getScriptName(id: Int): String? {
        val symbolMap = symbols[SymbolType.ScriptSymbol] ?: return null
        return symbolMap[id]
    }
}
