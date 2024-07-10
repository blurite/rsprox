package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.ZoneProt
import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties

/**
 * Loc add-change packed is used to either add or change a loc in the world.
 * The client will add a new loc if none exists by this description,
 * or overwrites an old one with the same layer (layer is obtained through the [shape]
 * property of the loc).
 * @property id the id of the loc to add
 * @property xInZone the x coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property shape the shape of the loc, a value of 0 to 22 (inclusive) is expected.
 * @property rotation the rotation of the loc, a value of 0 to 3 (inclusive) is expected.
 * @property opFlags the right-click options enabled on this loc.
 */
public class LocAddChange private constructor(
    private val _id: UShort,
    private val coordInZone: CoordInZone,
    private val locProperties: LocProperties,
    public val opFlags: OpFlags,
) : ZoneProt {
    public constructor(
        id: Int,
        xInZone: Int,
        zInZone: Int,
        shape: Int,
        rotation: Int,
        opFlags: OpFlags,
    ) : this(
        id.toUShort(),
        CoordInZone(xInZone, zInZone),
        LocProperties(shape, rotation),
        opFlags,
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
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override val protId: Int = OldSchoolZoneProt.LOC_ADD_CHANGE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocAddChange

        if (_id != other._id) return false
        if (coordInZone != other.coordInZone) return false
        if (locProperties != other.locProperties) return false
        if (opFlags != other.opFlags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + coordInZone.hashCode()
        result = 31 * result + locProperties.hashCode()
        result = 31 * result + opFlags.hashCode()
        return result
    }

    override fun toString(): String {
        return "LocAddChange(" +
            "id=$id, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone, " +
            "shape=$shape, " +
            "rotation=$rotation, " +
            "opFlags=$opFlags" +
            ")"
    }
}
