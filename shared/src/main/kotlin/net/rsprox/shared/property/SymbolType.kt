package net.rsprox.shared.property

import net.rsprox.shared.ScriptVarType

public sealed interface SymbolType {
    public data object VarpSymbol : SymbolType

    public data object VarbitSymbol : SymbolType

    public data object ScriptSymbol : SymbolType

    public data class ScriptVarTypeSymbol(
        public val type: ScriptVarType,
    ) : SymbolType
}
