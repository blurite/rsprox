package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone

/**
 * Map anim is sent to play a graphical effect/spotanim on a tile.
 * @property id the id of the spotanim
 * @property delay the delay in client cycles (20ms/cc) until the spotanim begins playing
 * @property height the height at which the spotanim will play
 * @property xInZone the x coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 */
public class MapAnim private constructor(
    private val _id: UShort,
    private val _delay: UShort,
    private val _height: UByte,
    private val coordInZone: CoordInZone,
) : IncomingZoneProt {
    public constructor(
        id: Int,
        delay: Int,
        height: Int,
        xInZone: Int,
        zInZone: Int,
    ) : this(
        id.toUShort(),
        delay.toUShort(),
        height.toUByte(),
        CoordInZone(xInZone, zInZone),
    )

    public constructor(
        id: Int,
        delay: Int,
        height: Int,
        coordInZone: CoordInZone,
    ) : this(
        id.toUShort(),
        delay.toUShort(),
        height.toUByte(),
        coordInZone,
    )

    public val id: Int
        get() = _id.toInt()
    public val delay: Int
        get() = _delay.toInt()
    public val height: Int
        get() = _height.toInt()
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()

    override val protId: Int = OldSchoolZoneProt.MAP_ANIM

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapAnim

        if (_id != other._id) return false
        if (_delay != other._delay) return false
        if (_height != other._height) return false
        if (coordInZone != other.coordInZone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _delay.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + coordInZone.hashCode()
        return result
    }

    override fun toString(): String {
        return "MapAnim(" +
            "id=$id, " +
            "delay=$delay, " +
            "height=$height, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
    }
}
