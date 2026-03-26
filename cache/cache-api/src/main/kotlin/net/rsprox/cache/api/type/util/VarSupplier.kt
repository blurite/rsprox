package net.rsprox.cache.api.type.util

import net.rsprox.cache.api.type.VarBitType

public interface VarSupplier {
    public fun getVarps(): IntArray

    public fun getVarBitType(id: Int): VarBitType?
}
