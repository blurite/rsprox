package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea

/**
 * Map anim specific is sent to play a graphical effect/spotanim on a tile,
 * local to a single user, and not the entire world.
 * @property id the id of the spotanim
 * @property delay the delay in client cycles (20ms/cc) until the spotanim begins playing
 * @property height the height at which the spotanim will play
 * @property zoneX the x coordinate of the zone's south-western corner in the
 * build area.
 * @property xInZone the x coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zoneZ the z coordinate of the zone's south-western corner in the
 * build area.
 * @property zInZone the z coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 *
 * It should be noted that the [zoneX] and [zoneZ] coordinates are relative
 * to the build area in their absolute form, not in their shifted zone form.
 * If the player is at an absolute coordinate of 50, 40 within the build area(104x104),
 * the expected coordinates to transmit here would be 48, 40, as that would
 * point to the south-western corner of the zone in which the player is standing in.
 * The client will add up the respective [zoneX] + [xInZone] properties together,
 * along with [zoneZ] + [zInZone] to re-create the effects of a normal zone packet.
 */
public class MapAnimSpecific private constructor(
    private val _id: UShort,
    private val _delay: UShort,
    private val _height: UByte,
    public val coordInBuildArea: CoordInBuildArea,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        delay: Int,
        height: Int,
        zoneX: Int,
        xInZone: Int,
        zoneZ: Int,
        zInZone: Int,
    ) : this(
        id.toUShort(),
        delay.toUShort(),
        height.toUByte(),
        CoordInBuildArea(
            zoneX,
            xInZone,
            zoneZ,
            zInZone,
        ),
    )

    public constructor(
        id: Int,
        delay: Int,
        height: Int,
        xInBuildArea: Int,
        zInBuildArea: Int,
    ) : this(
        id.toUShort(),
        delay.toUShort(),
        height.toUByte(),
        CoordInBuildArea(
            xInBuildArea,
            zInBuildArea,
        ),
    )

    internal constructor(
        id: Int,
        delay: Int,
        height: Int,
        coordInBuildArea: CoordInBuildArea,
    ) : this(
        id.toUShort(),
        delay.toUShort(),
        height.toUByte(),
        coordInBuildArea,
    )

    public val id: Int
        get() = _id.toInt()
    public val delay: Int
        get() = _delay.toInt()
    public val height: Int
        get() = _height.toInt()
    public val zoneX: Int
        get() = coordInBuildArea.zoneX
    public val xInZone: Int
        get() = coordInBuildArea.xInZone
    public val zoneZ: Int
        get() = coordInBuildArea.zoneZ
    public val zInZone: Int
        get() = coordInBuildArea.zInZone

    public val coordInBuildAreaPacked: Int
        get() = coordInBuildArea.packedMedium

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapAnimSpecific

        if (_id != other._id) return false
        if (_delay != other._delay) return false
        if (_height != other._height) return false
        if (coordInBuildArea != other.coordInBuildArea) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _delay.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + coordInBuildArea.hashCode()
        return result
    }

    override fun toString(): String {
        return "MapAnimSpecific(" +
            "id=$id, " +
            "delay=$delay, " +
            "height=$height, " +
            "zoneX=$zoneX, " +
            "xInZone=$xInZone, " +
            "zoneZ=$zoneZ, " +
            "zInZone=$zInZone" +
            ")"
    }
}
