package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.ZoneProt
import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone

/**
 * Obj uncustomise resets any customisations done to an obj via the [ObjCustomise] packet.
 * @property id the id of the obj to update
 * @property quantity the quantity of the obj to update
 * @property xInZone the x coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 */
public class ObjUncustomise private constructor(
    private val _id: UShort,
    public val quantity: Int,
    private val coordInZone: CoordInZone,
) : IncomingZoneProt {
    public constructor(
        id: Int,
        quantity: Int,
        xInZone: Int,
        zInZone: Int,
    ) : this(
        id.toUShort(),
        quantity,
        CoordInZone(xInZone, zInZone),
    )

    public val id: Int
        get() = _id.toInt()
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    override val protId: Int = OldSchoolZoneProt.OBJ_UNCUSTOMISE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjUncustomise) return false

        if (_id != other._id) return false
        if (quantity != other.quantity) return false
        if (coordInZone != other.coordInZone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + quantity
        result = 31 * result + coordInZone.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjCustomise(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
    }
}
