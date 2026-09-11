package net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util

public enum class Rs3NpcUpdateMaskKey(public val bit: Int, public val order: Int) {
    SAY(6, 1),
    NPC_FREEZE(0, 2),
    TRANSPARENCY(34, 3),
    TRACKED_FACE_LOCK(12, 4),
    HEAD_CUSTOMISATION(20, 5),
    TRANSIENT_BOOL(26, 6),
    TIMED_EFFECT_1(29, 7),
    TINTING(28, 8),
    TRANSFORMATION(2, 9),
    EXACT_MOVE(14, 10),
    CONFIG_PARAMS_2(21, 11),
    SECONDARY_FREEZE(8, 12),
    CONFIG_PARAMS_1(16, 13),
    NPC_STATS(19, 14),
    NAME_CHANGE(18, 15),
    ENABLED_OPS(15, 16),
    FACE_ENTITY(1, 17),
    FACE_TILE(7, 18),
    TIMED_EFFECT_3(31, 19),
    SCALE_CHANGE(11, 20),
    BONE_TRANSFORMS(32, 21),
    BODY_CUSTOMISATION(10, 22),
    SPOTANIM(24, 23),
    HITMARKS_AND_HEADBARS_WIDE(33, 24),
    COMBAT_LEVEL_CHANGE(17, 25),
    SEQUENCE(3, 26),
    TINTING_CHANNELS(22, 27),
    HITMARKS_AND_HEADBARS(5, 28),
    VISIBILITY_FLAG(25, 29),
    TIMED_EFFECT_2(30, 30),
    ;

    public companion object {
        public val byOrder: List<Rs3NpcUpdateMaskKey> = entries.sortedBy { it.order }
        public val byBit: Map<Int, Rs3NpcUpdateMaskKey> = entries.associateBy { it.bit }

        public val EXPANSION_BITS: IntArray = intArrayOf(4, 13, 23, 27)

        public val DECODABLE: Set<Rs3NpcUpdateMaskKey> = entries.toSet()
        public val UNVERIFIED: Set<Rs3NpcUpdateMaskKey> = emptySet()
    }
}
