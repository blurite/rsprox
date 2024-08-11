package net.rsprox.shared.indexing

public enum class IndexedType(
    public val id: Int,
) {
    NPC(0),
    OBJ(1),
    LOC(2),
    MAPSQUARE(3),
    MESSAGE_GAME(4),
}
