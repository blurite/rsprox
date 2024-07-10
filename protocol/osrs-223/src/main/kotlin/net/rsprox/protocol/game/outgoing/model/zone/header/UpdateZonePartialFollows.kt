package net.rsprox.protocol.game.outgoing.model.zone.header

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update zone partial follows packets are used to set the 'current zone pointer' to this
 * zone, allowing one to follow it with any other zone payload packet.
 * This packet is more efficient to use over the partial-enclosed variant
 * when there is only a single zone packet following it, in any other scenario,
 * it is more bandwidth-friendly to use the enclosed packet.
 * @property zoneX the x coordinate of the zone's south-western corner in the
 * build area.
 * @property zoneZ the z coordinate of the zone's south-western corner in the
 * build area.
 * @property level the height level of the zone, typically equal to the player's
 * own height level.
 *
 * It should be noted that the [zoneX] and [zoneZ] coordinates are relative
 * to the build area in their absolute form, not in their shifted zone form.
 * If the player is at an absolute coordinate of 50, 40 within the build area(104x104),
 * the expected coordinates to transmit here would be 48, 40, as that would
 * point to the south-western corner of the zone in which the player is standing in.
 */
public class UpdateZonePartialFollows private constructor(
    private val _zoneX: UByte,
    private val _zoneZ: UByte,
    private val _level: UByte,
) : IncomingServerGameMessage {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        level: Int,
    ) : this(
        zoneX.toUByte(),
        zoneZ.toUByte(),
        level.toUByte(),
    )

    public val zoneX: Int
        get() = _zoneX.toInt()
    public val zoneZ: Int
        get() = _zoneZ.toInt()
    public val level: Int
        get() = _level.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateZonePartialFollows

        if (_zoneX != other._zoneX) return false
        if (_zoneZ != other._zoneZ) return false
        if (_level != other._level) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _zoneX.hashCode()
        result = 31 * result + _zoneZ.hashCode()
        result = 31 * result + _level.hashCode()
        return result
    }

    override fun toString(): String {
        return "UpdateZonePartialFollows(" +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "level=$level" +
            ")"
    }
}
