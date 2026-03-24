package net.rsprox.protocol.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea

/**
 * Rebuild region is used to send a dynamic map to the client,
 * built up out of zones (8x8x1 tiles), allowing for any kind
 * of unique instancing to occur.
 * @property zoneX the x coordinate of the center zone around
 * which the build area is built
 * @property zoneZ the z coordinate of the center zone around
 * which the build area is built
 * @property reload whether to forcibly reload the map client-sided.
 * If this property is false, the client will only reload if
 * the last rebuild had difference [zoneX] or [zoneZ] coordinates
 * than this one.
 * @property buildArea the build area consisting of all the zones
 */
public class RebuildRegionV2 private constructor(
    private val _zoneX: UShort,
    private val _zoneZ: UShort,
    public val reload: Boolean,
    public val buildArea: BuildArea,
) : IncomingServerGameMessage {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        reload: Boolean,
        buildArea: BuildArea,
    ) : this(
        zoneX.toUShort(),
        zoneZ.toUShort(),
        reload,
        buildArea,
    )

    public val zoneX: Int
        get() = _zoneX.toInt()
    public val zoneZ: Int
        get() = _zoneZ.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RebuildRegionV2

        if (_zoneX != other._zoneX) return false
        if (_zoneZ != other._zoneZ) return false
        if (reload != other.reload) return false
        if (buildArea != other.buildArea) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _zoneX.hashCode()
        result = 31 * result + _zoneZ.hashCode()
        result = 31 * result + reload.hashCode()
        result = 31 * result + buildArea.hashCode()
        return result
    }

    override fun toString(): String {
        return "RebuildRegionV2(" +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "reload=$reload, " +
            "buildArea=$buildArea" +
            ")"
    }
}
