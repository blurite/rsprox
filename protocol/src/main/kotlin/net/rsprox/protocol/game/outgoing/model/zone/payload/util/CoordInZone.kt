package net.rsprox.protocol.game.outgoing.model.zone.payload.util

@JvmInline
public value class CoordInZone private constructor(
    public val packed: UByte,
) {
    public constructor(
        xInZone: Int,
        zInZone: Int,
    ) : this(
        (xInZone and 0x7 shl 4)
            .or(zInZone and 0x7)
            .toUByte(),
    )

    public constructor(packed: Int) : this(packed.toUByte())

    public val xInZone: Int
        get() = packed.toInt() ushr 4 and 0x7
    public val zInZone: Int
        get() = packed.toInt() and 0x7

    override fun toString(): String {
        return "CoordInZone(" +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
    }
}
