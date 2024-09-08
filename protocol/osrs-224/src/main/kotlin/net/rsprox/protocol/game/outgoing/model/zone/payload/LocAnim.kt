package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties

/**
 * Loc anim packets are used to make a loc play an animation.
 * @property id the id of the animation to play
 * @property xInZone the x coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property shape the shape of the loc, a value of 0 to 22 (inclusive) is expected.
 * @property rotation the rotation of the loc, a value of 0 to 3 (inclusive) is expected.
 */
public class LocAnim private constructor(
    private val _id: UShort,
    private val coordInZone: CoordInZone,
    private val locProperties: LocProperties,
) : IncomingZoneProt {
    public constructor(
        id: Int,
        xInZone: Int,
        zInZone: Int,
        shape: Int,
        rotation: Int,
    ) : this(
        id.toUShort(),
        CoordInZone(xInZone, zInZone),
        LocProperties(shape, rotation),
    )

    internal constructor(
        id: Int,
        coordInZone: CoordInZone,
        locProperties: LocProperties,
    ) : this(
        id.toUShort(),
        coordInZone,
        locProperties,
    )

    public val id: Int
        get() = _id.toInt()
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone
    public val shape: Int
        get() = locProperties.shape
    public val rotation: Int
        get() = locProperties.rotation

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    public val locPropertiesPacked: Int
        get() = locProperties.packed.toInt()

    override val protId: Int = OldSchoolZoneProt.LOC_ANIM

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocAnim

        if (_id != other._id) return false
        if (coordInZone != other.coordInZone) return false
        if (locProperties != other.locProperties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + coordInZone.hashCode()
        result = 31 * result + locProperties.hashCode()
        return result
    }

    override fun toString(): String {
        return "LocAnim(" +
            "id=$id, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone, " +
            "shape=$shape, " +
            "rotation=$rotation" +
            ")"
    }
}
