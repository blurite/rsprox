package net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.util

public enum class Rs3NpcUpdateMaskKey(public val bit: Int, public val order: Int) {
    CLIENT_SCRIPT_OVERRIDE(20, 1),
    MODEL_OVERRIDE_ID(22, 2),
    UNK_BIT4(4, 3),
    TRANSIENT_BOOL(28, 4),
    ANIMATION(3, 5),
    UNK_BIT1(1, 6),
    HITMARKS_AND_HEADBARS(0, 7),
    FACE_ENTITY(7, 8),
    VISIBILITY_FLAG(25, 9),
    UNK_BIT9(9, 10),
    UNK_BIT21(21, 11),
    COMBAT_LEVEL_OVERRIDE_RGB(31, 12),
    FORCED_MOVEMENT(11, 13),
    NPC_STATS(16, 14),
    UNK_BIT27(27, 15),
    UNK_BIT33(33, 16),
    UNK_BIT30(30, 17),
    SPOT_ANIM_LIST_BIT19(19, 18),
    FACE_TILE(6, 19),
    STRING_OVERRIDE(2, 20),
    UNK_BIT13(13, 21),
    UNK_BIT34(34, 22),
    UNK_BIT29(29, 23),
    HITMARKS_AND_HEADBARS_2(32, 24),
    UNK_BIT8(8, 25),
    SPOT_ANIM_LIST_BIT23(23, 26),
    NAME_OVERRIDE(17, 27),
    UNK_BIT24(24, 28),
    UNK_BIT12(12, 29),
    TRACKED_FACE_LOCK(10, 30),
    ;

    public companion object {
        public val byOrder: List<Rs3NpcUpdateMaskKey> = entries.sortedBy { it.order }
        public val byBit: Map<Int, Rs3NpcUpdateMaskKey> = entries.associateBy { it.bit }
        public val EXPANSION_BITS: IntArray = intArrayOf(5, 14, 18, 26)

        public val DECODABLE: Set<Rs3NpcUpdateMaskKey> =
            setOf(
                VISIBILITY_FLAG, TRANSIENT_BOOL, MODEL_OVERRIDE_ID, ANIMATION,
                FACE_TILE, FACE_ENTITY, TRACKED_FACE_LOCK, COMBAT_LEVEL_OVERRIDE_RGB, NAME_OVERRIDE,
                STRING_OVERRIDE, NPC_STATS,
                UNK_BIT8, FORCED_MOVEMENT, UNK_BIT27, UNK_BIT13, UNK_BIT34, UNK_BIT29, UNK_BIT24,
                UNK_BIT12, UNK_BIT1, UNK_BIT21, SPOT_ANIM_LIST_BIT19, SPOT_ANIM_LIST_BIT23,
                UNK_BIT33,
            )

        public val UNVERIFIED: Set<Rs3NpcUpdateMaskKey> =
            setOf(
                CLIENT_SCRIPT_OVERRIDE, UNK_BIT9, UNK_BIT30,
                HITMARKS_AND_HEADBARS, HITMARKS_AND_HEADBARS_2,
            )
    }
}
