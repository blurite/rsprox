package net.rsprox.cache.api.rs3

/** Names from one cache snapshot, shared by its decoders and transcript formatters. */
public class Rs3Gamevals(
    names: Map<Rs3Gameval, Map<Int, String>>,
) {
    private val names = names.mapValues { (_, entries) -> entries.toMap() }

    public operator fun get(
        type: Rs3Gameval,
        id: Int,
    ): String? = names[type]?.get(id)

    public companion object {
        public val EMPTY: Rs3Gamevals = Rs3Gamevals(emptyMap())
    }
}

public enum class Rs3Gameval(
    public val group: Int,
) {
    COMPONENT(0),
    BAS(5),
    CATEGORY(9),
    CURSOR(12),
    DBROW(14),
    DBTABLE(15),
    ENUM(16),
    HEADBAR(20),
    HITMARK(21),
    INTERFACE(24),
    INV(25),
    LOC(28),
    MAPELEMENT(29),
    MATERIAL(32),
    MODEL(34),
    NPC(35),
    OBJ(36),
    PARAM(37),
    QUEST(41),
    SEQ(44),
    GRAPHIC(49),
    STRUCT(50),
    VAR_CLAN(55),
    VAR_CLAN_SETTING(56),
    VARC(57),
    VARNPC(59),
    VAROBJ(60),
    VARP(61),
    SOUND(64),
    MIDI(69),
    VAR_PLAYER_GROUP(80),
    ACHIEVEMENT(89),
    FONTMETRICS(90),
    STYLESHEET(92),
    UI_ANIM_CURVE(96),
    UI_ANIM(97),
    // Embedded in the variable-name groups, rather than a separate group.
    VARBIT(-1),
}
