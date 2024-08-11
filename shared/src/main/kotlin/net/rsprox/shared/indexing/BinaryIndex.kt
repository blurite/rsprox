package net.rsprox.shared.indexing

public interface BinaryIndex {
    public operator fun get(
        type: IndexedType,
        key: IndexedKey,
    ): Int

    public operator fun set(
        type: IndexedType,
        key: IndexedKey,
        count: Int,
    )

    public fun increment(
        type: IndexedType,
        key: IndexedKey,
    ) {
        set(type, key, get(type, key) + 1)
    }

    public fun results(): Map<IndexedType, Map<IndexedKey, Int>>
}
