package net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util

public enum class Rs3PlayerUpdateMaskKey(public val bit: Int, public val order: Int) {
    SPOTANIM(26, 1),
    UNUSED_MASK_8(8, 2),
    UNUSED_MASK_20(20, 3),
    SEQUENCE(3, 4),
    CLAN_MEMBER(23, 5),
    FACE_ENTITY(7, 6),
    UNUSED_MASK_12(12, 7),
    UNUSED_MASK_24(24, 8),
    VARP_DELTA(19, 9),
    HITMARKS_AND_HEADBARS_V1(6, 10),
    HEAD_ICONS(11, 11),
    SAY_V2(22, 12),
    APPEARANCE(5, 13),
    EXACT_MOVE(0, 14),
    VARP_FULL(17, 15),
    TINTING(21, 16),
    UNUSED_MASK_16(16, 17),
    HITMARKS_AND_HEADBARS_V2(25, 18),
    ATTACHMENTS(27, 19),
    PLAYER_STATUS(9, 20),
    SAY_V1(10, 21),
    FACE_ANGLE(1, 22),
    UNUSED_MASK_2(2, 23),
    UNUSED_MASK_13(13, 24),
    ;

    public companion object {
        public val byOrder: List<Rs3PlayerUpdateMaskKey> = entries.sortedBy { it.order }
        public val byBit: Map<Int, Rs3PlayerUpdateMaskKey> = entries.associateBy { it.bit }

        public val EXPANSION_BITS: IntArray = intArrayOf(4, 15, 18)

        public val DECODABLE: Set<Rs3PlayerUpdateMaskKey> = entries.toSet()
    }
}
