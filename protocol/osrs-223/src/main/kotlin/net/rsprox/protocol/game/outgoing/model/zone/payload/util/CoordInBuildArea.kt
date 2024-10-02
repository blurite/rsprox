package net.rsprox.protocol.game.outgoing.model.zone.payload.util

/**
 * Coord in build-area is a helper class to compress the data used to transmit
 * build-area coords to the client, primarily in *-specific packets.
 * These packets will separate the south-western zone X/Z coordinates,
 * and the x/z in-zone coordinates into separate properties.
 * @property zoneX the south-western x coordinate of the zone (multiples of 8 value)
 * @property xInZone the x coordinate within the zone (0-7 value)
 * @property zoneZ the south-western z coordinate of the zone (multiples of 8 value)
 * @property zInZone the z coordinate within the zone (0-7 value)
 * @property packedMedium the coordinates bitpacked into a 24-bit integer,
 * as this is how they tend to be transmitted to the client.
 */
@JvmInline
public value class CoordInBuildArea private constructor(
    private val packedShort: UShort,
) {
    public constructor(packed: Int) : this(
        packed ushr 16 and 0xFF,
        packed ushr 4 and 0x7,
        packed ushr 8 and 0xFF,
        packed and 0x7,
    )

    public constructor(
        zoneX: Int,
        xInZone: Int,
        zoneZ: Int,
        zInZone: Int,
    ) : this(
        ((zoneX and 0x7.inv() or (xInZone and 0x7)) shl 8)
            .or((zoneZ and 0x7.inv()) or (zInZone and 0x7))
            .toUShort(),
    )

    public constructor(
        xinBuildArea: Int,
        zInBuildArea: Int,
    ) : this(
        ((xinBuildArea and 0xFF) shl 8)
            .or(zInBuildArea)
            .toUShort(),
    )

    public val zoneX: Int
        get() = packedShort.toInt() ushr 8 and 0xF8
    public val xInZone: Int
        get() = packedShort.toInt() ushr 8 and 0x7
    public val zoneZ: Int
        get() = packedShort.toInt() and 0xF8
    public val zInZone: Int
        get() = packedShort.toInt() and 0x7

    public val xInBuildArea: Int
        get() = packedShort.toInt() ushr 8
    public val zInBuildArea: Int
        get() = packedShort.toInt() and 0xFF

    public val packedMedium: Int
        get() =
            (zoneX shl 16)
                .or(zoneZ shl 8)
                .or(xInZone shl 4)
                .or(zInZone)

    override fun toString(): String {
        return "CoordInBuildArea(" +
            "xInBuildArea=$xInBuildArea, " +
            "zInBuildArea=$zInBuildArea" +
            ")"
    }
}
