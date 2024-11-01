package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone

/**
 * Obj enabled ops is used to change the right-click options on an obj
 * on the ground. This packet is currently unused in OldSchool RuneScape.
 * It works by finding the first obj in the stack with the provided [id],
 * and modifying the right-click ops on that. It does not verify quantity.
 * @property id the id of the obj that needs to get its ops changed
 * @property opFlags the right-click options to set enabled on that obj
 * @property xInZone the x coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 */
public class ObjEnabledOps private constructor(
    private val _id: UShort,
    public val opFlags: OpFlags,
    private val coordInZone: CoordInZone,
) : IncomingZoneProt {
    public constructor(
        id: Int,
        opFlags: OpFlags,
        xInZone: Int,
        zInZone: Int,
    ) : this(
        id.toUShort(),
        opFlags,
        CoordInZone(xInZone, zInZone),
    )

    internal constructor(
        id: Int,
        opFlags: OpFlags,
        coordInZone: CoordInZone,
    ) : this(
        id.toUShort(),
        opFlags,
        coordInZone,
    )

    public val id: Int
        get() = _id.toInt()
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    override val protId: Int = OldSchoolZoneProt.OBJ_ENABLED_OPS

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjEnabledOps

        if (_id != other._id) return false
        if (opFlags != other.opFlags) return false
        if (coordInZone != other.coordInZone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + opFlags.hashCode()
        result = 31 * result + coordInZone.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjEnabledOps(" +
            "id=$id, " +
            "opFlags=$opFlags, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
    }
}
