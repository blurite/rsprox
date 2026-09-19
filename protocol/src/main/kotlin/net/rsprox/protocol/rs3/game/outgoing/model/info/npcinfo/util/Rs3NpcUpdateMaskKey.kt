package net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util

public enum class Rs3NpcUpdateMaskKey(public val bit: Int, public val order: Int) {
    SAY(6, 1),
    UNUSED_MASK_0(0, 2),
    PRIORITY_OFFSET(34, 3),
    BAS_OVERRIDE(12, 4),
    HEAD_CUSTOMISATION(20, 5),
    OVERLAP_CULLING(26, 6),
    UNUSED_MASK_29(29, 7),
    TINTING(28, 8),
    TRANSFORMATION(2, 9),
    EXACT_MOVE(14, 10),
    VARNPC_FULL(21, 11),
    UNUSED_MASK_8(8, 12),
    VARNPC_DELTA(16, 13),
    NPC_STATS(19, 14),
    NAME_CHANGE(18, 15),
    UNUSED_MASK_15(15, 16),
    FACE_ENTITY(1, 17),
    FACE_TILE(7, 18),
    UNUSED_MASK_31(31, 19),
    UNUSED_MASK_11(11, 20),
    ATTACHMENTS(32, 21),
    BODY_CUSTOMISATION(10, 22),
    SPOTANIM(24, 23),
    HITMARKS_AND_HEADBARS_V2(33, 24),
    COMBAT_LEVEL_CHANGE(17, 25),
    SEQUENCE(3, 26),
    HEADICON_CUSTOMISATION(22, 27),
    HITMARKS_AND_HEADBARS_V1(5, 28),
    DISABLED_OPS(25, 29),
    UNUSED_MASK_30(30, 30),
    ;

    public companion object {
        public val byOrder: List<Rs3NpcUpdateMaskKey> = entries.sortedBy { it.order }
        public val byBit: Map<Int, Rs3NpcUpdateMaskKey> = entries.associateBy { it.bit }

        public val EXPANSION_BITS: IntArray = intArrayOf(4, 13, 23, 27)

        public val DECODABLE: Set<Rs3NpcUpdateMaskKey> = entries.toSet()
        public val UNVERIFIED: Set<Rs3NpcUpdateMaskKey> = emptySet()
    }
}
