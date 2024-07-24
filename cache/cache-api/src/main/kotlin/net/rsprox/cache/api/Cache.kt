package net.rsprox.cache.api

import net.rsprox.cache.api.type.NpcType
import net.rsprox.cache.api.type.VarBitType

public interface Cache {
    public fun getNpcType(id: Int): NpcType?

    public fun getVarBitType(id: Int): VarBitType?
}
