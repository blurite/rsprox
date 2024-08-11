package net.rsprox.shared.indexing

public enum class IndexedType(
    public val id: Int,
) {
    NPC(0),
    OBJ(1),
    LOC(2),
    MAPSQUARE(3),
    MESSAGE_GAME(4),
    VARP(5),
    VARBIT(6),
    INTERFACE(7),
    SEQ(8),
    SPOTANIM(9),
    MIDI(10),
    SYNTH(11),
    JINGLE(12),
    CLIENTSCRIPTS(13),
    TEXT(14),
}
