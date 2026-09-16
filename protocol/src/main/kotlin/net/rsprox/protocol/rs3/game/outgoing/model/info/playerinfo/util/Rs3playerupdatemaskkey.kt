package net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util

public enum class Rs3PlayerUpdateMaskKey(public val bit: Int, public val order: Int) {
    SPOTANIM(26, 1),
    ENABLED_OPS(8, 2),
    SECONDARY_FREEZE(20, 3),
    SEQUENCE(3, 4),
    PRIORITY_FLAG(23, 5),
    FACE_ENTITY(7, 6),
    PLAYER_FREEZE(12, 7),
    TIMED_EFFECT_1(24, 8),
    CONFIG_PARAMS_1(19, 9),
    HITMARKS_AND_HEADBARS(6, 10),
    HEAD_ICONS(11, 11),
    NAME_EXTRAS(22, 12),
    APPEARANCE(5, 13),
    EXACT_MOVE(0, 14),
    CONFIG_PARAMS_2(17, 15),
    TINTING(21, 16),
    SCALE_CHANGE(16, 17),
    HITMARKS_AND_HEADBARS_WIDE(25, 18),
    BONE_TRANSFORMS(27, 19),
    TRANSPARENCY(9, 20),
    SAY(10, 21),
    HEAD_TURN_ANGLE(1, 22),
    TIMED_EFFECT_2(2, 23),
    TIMED_EFFECT_3(13, 24),
    ;

    public companion object {
        public val byOrder: List<Rs3PlayerUpdateMaskKey> = entries.sortedBy { it.order }
        public val byBit: Map<Int, Rs3PlayerUpdateMaskKey> = entries.associateBy { it.bit }

        public val EXPANSION_BITS: IntArray = intArrayOf(4, 15, 18)

        public val DECODABLE: Set<Rs3PlayerUpdateMaskKey> = entries.toSet()
    }
}
