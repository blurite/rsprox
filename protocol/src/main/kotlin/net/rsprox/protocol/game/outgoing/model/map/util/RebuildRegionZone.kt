package net.rsprox.protocol.game.outgoing.model.map.util

@JvmInline
public value class RebuildRegionZone(
    public val packed: Int,
) {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        level: Int,
        rotation: Int,
    ) : this(
        ((rotation and 0x3) shl 1)
            .or((zoneZ and 0x7FF) shl 3)
            .or((zoneX and 0x3FF) shl 14)
            .or((level and 0x3) shl 24),
    )

    public val rotation: Int
        get() = packed ushr 1 and 0x3
    public val zoneX: Int
        get() = packed ushr 14 and 0x3FF
    public val zoneZ: Int
        get() = packed ushr 3 and 0x7FF
    public val level: Int
        get() = packed ushr 24 and 0x3
    public val invalid: Boolean
        get() = packed == -1

    public val mapsquareId: Int
        get() = ((zoneX ushr 3) shl 8) or (zoneZ ushr 3)

    override fun toString(): String {
        return "RebuildRegionZone(" +
            "rotation=$rotation, " +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "level=$level" +
            ")"
    }
}
