package net.rsprox.shared.indexing

public fun BinaryIndex.increment(
    type: IndexedType,
    key: Int,
) {
    val typedKey = IndexedKey.IntKey(key)
    increment(type, typedKey)
}

public fun BinaryIndex.increment(
    type: IndexedType,
    key: String,
) {
    val typedKey = IndexedKey.StringKey(key)
    increment(type, typedKey)
}
