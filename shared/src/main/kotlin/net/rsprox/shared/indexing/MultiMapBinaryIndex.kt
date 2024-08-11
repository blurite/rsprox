package net.rsprox.shared.indexing

public class MultiMapBinaryIndex : BinaryIndex {
    private val map: MutableMap<IndexedType, MutableMap<IndexedKey, Int>> = mutableMapOf()

    override fun get(
        type: IndexedType,
        key: IndexedKey,
    ): Int {
        return map[type]?.get(key) ?: 0
    }

    override fun set(
        type: IndexedType,
        key: IndexedKey,
        count: Int,
    ) {
        map.getOrPut(type, ::mutableMapOf)[key] = count
    }

    override fun results(): Map<IndexedType, Map<IndexedKey, Int>> {
        return map
    }
}
