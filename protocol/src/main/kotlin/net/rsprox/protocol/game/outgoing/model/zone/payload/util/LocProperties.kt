package net.rsprox.protocol.game.outgoing.model.zone.payload.util

@JvmInline
public value class LocProperties private constructor(
    public val packed: UByte,
) {
    public constructor(packed: Int) : this(packed.toUByte())

    public constructor(
        shape: Int,
        rotation: Int,
    ) : this(
        (shape and 0x1F shl 2)
            .or(rotation and 0x3)
            .toUByte(),
    )

    public val shape: Int
        get() = packed.toInt() ushr 2 and 0x1F
    public val rotation: Int
        get() = packed.toInt() and 0x3

    override fun toString(): String {
        return "LocProperties(" +
            "shape=$shape, " +
            "rotation=$rotation" +
            ")"
    }
}
