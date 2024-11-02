package net.rsprox.protocol.game.incoming.model.misc.user.internal

/**
 * A value class around an int to bitpack all the properties of move gameclick
 * into a single integer. This is primarily to constrain our class to a payload
 * of 4 bytes, as going above it means being subject to increased memory alignment.
 * As mentioned in documentation before, empty objects allocate 12 bytes, but
 * get aligned to a multiple of 8 bytes - so they will consume 16 bytes.
 * Putting an int inside the class would allocate 16 bytes, and remain as 16
 * after padding. Allocating 17 bytes (ref: 12, x: 2, y: 2, key: 1),
 * however, would mean the class is subject to being padded to 24 bytes, with 7 of
 * them being completely wasted in the process.
 */
@JvmInline
internal value class MovementRequest private constructor(
    private val packed: Int,
) {
    internal constructor(
        x: Int,
        z: Int,
        keyCombination: Int,
    ) : this(
        (z and 0x3FFF)
            .or(x and 0x3FFF shl 14)
            .or(keyCombination and 0x3 shl 28),
    )

    val x: Int
        get() = packed ushr 14 and 0x3FFF
    val z: Int
        get() = packed and 0x3FFF
    val keyCombination: Int
        get() = packed ushr 28 and 0x3
}
