package net.rsprox.shared.indexing

public data object NopBinaryIndex : BinaryIndex {
    override fun get(
        type: IndexedType,
        key: IndexedKey,
    ): Int {
        return 0
    }

    override fun set(
        type: IndexedType,
        key: IndexedKey,
        count: Int,
    ) {
    }

    override fun results(): Map<IndexedType, Map<IndexedKey, Int>> {
        return emptyMap()
    }
}
