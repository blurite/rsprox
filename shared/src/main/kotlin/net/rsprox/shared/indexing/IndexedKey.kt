package net.rsprox.shared.indexing

public sealed interface IndexedKey {
    public data class IntKey(
        public val value: Int,
    ) : IndexedKey

    public data class StringKey(
        public val value: String,
    ) : IndexedKey
}
