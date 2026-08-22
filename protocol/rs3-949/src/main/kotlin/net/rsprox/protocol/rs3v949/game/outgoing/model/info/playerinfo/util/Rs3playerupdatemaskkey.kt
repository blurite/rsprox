package net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.util

public enum class Rs3PlayerUpdateMaskKey(public val bit: Int, public val order: Int) {
    UNK_BIT13(13, 1), // mask 0x2000
    NAME_BLOCK_A(10, 2), // mask 0x400
    RAW_BLOB_A(12, 3), // mask 0x1000
    UNK_BIT7(7, 4), // mask 0x80
    TRANSFORM_BLEND(26, 5), // mask 0x4000000
    HITMARKS_AND_HEADBARS(4, 6), // mask 0x10
    UNK_BIT9(9, 7), // mask 0x200
    UNK_BIT11(11, 8), // mask 0x800
    APPEARANCE(2, 9), // mask 4
    UNK_BIT3(3, 10), // mask 8
    ANIMATION(1, 11), // mask 2
    UNK_BIT23(23, 12), // mask 0x800000
    PARAMS_A(24, 13), // mask 0x1000000
    UNK_BIT27(27, 14), // mask 0x8000000
    NAME_BLOCK_B(18, 15), // mask 0x40000
    PARAMS_B(19, 16), // mask 0x80000
    FORCED_MOVEMENT(6, 17), // mask 0x40
    COMBAT_LEVEL_OVERRIDE_RGB(20, 18), // mask 0x100000
    FACE_ENTITY(8, 19), // mask 0x100
    PARAMS_C(22, 20), // mask 0x400000
    UNK_BIT16(16, 21), // mask 0x10000
    OVERHEAD_TEXT(0, 22), // mask 1
    HITMARKS_AND_HEADBARS_2(25, 23), // mask 0x2000000
    TRANSIENT_BOOL(21, 24), // mask 0x200000
    UNK_BIT5(5, 25), // mask 0x20 - MECHANICAL
    UNK_BIT14(14, 26), // mask 0x4000 - MECHANICAL
    UNK_BIT15(15, 27), // mask 0x8000
    UNK_BIT17(17, 28), // mask 0x20000 - MECHANICAL
    ;

    public companion object {
        public val byOrder: List<Rs3PlayerUpdateMaskKey> = entries.sortedBy { it.order }
        public val byBit: Map<Int, Rs3PlayerUpdateMaskKey> = entries.associateBy { it.bit }

        public val EXPANSION_BITS: IntArray = intArrayOf(5, 14, 17)

        public val DECODABLE: Set<Rs3PlayerUpdateMaskKey> =
            setOf(
                UNK_BIT13, UNK_BIT7, TRANSFORM_BLEND, UNK_BIT9, UNK_BIT11, UNK_BIT3, UNK_BIT23,
                UNK_BIT27, FORCED_MOVEMENT, COMBAT_LEVEL_OVERRIDE_RGB, FACE_ENTITY, UNK_BIT16,
                OVERHEAD_TEXT, HITMARKS_AND_HEADBARS_2, TRANSIENT_BOOL, ANIMATION,
                RAW_BLOB_A, APPEARANCE, NAME_BLOCK_A, NAME_BLOCK_B,
                PARAMS_A, PARAMS_B, PARAMS_C,
            )

        public val MECHANICAL: Set<Rs3PlayerUpdateMaskKey> = setOf(UNK_BIT5, UNK_BIT14, UNK_BIT15, UNK_BIT17)

        public val UNVERIFIED: Set<Rs3PlayerUpdateMaskKey> = emptySet()
    }
}
