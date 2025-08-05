package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.util.OpFlags

/**
 * Obj enabled ops is used to change the right-click options on an obj
 * on the ground. This packet is currently unused in OldSchool RuneScape.
 * It works by finding the first obj in the stack with the provided [id],
 * and modifying the right-click ops on that. It does not verify quantity.
 * @property id the id of the obj that needs to get its ops changed
 * @property opFlags the right-click options to set enabled on that obj.
 * Use the [net.rsprot.protocol.game.outgoing.util.OpFlags] helper object to create these
 * bitpacked values which can be passed into it.
 * @property coordGrid the absolute coordinate at which the obj is modified.
 */
public class ObjEnabledOpsSpecific private constructor(
    private val _id: UShort,
    public val opFlags: OpFlags,
    public val coordGrid: CoordGrid,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        opFlags: OpFlags,
        coordGrid: CoordGrid,
    ) : this(
        id.toUShort(),
        opFlags,
        coordGrid,
    )

    public val id: Int
        get() = _id.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjEnabledOpsSpecific

        if (_id != other._id) return false
        if (opFlags != other.opFlags) return false
        if (coordGrid != other.coordGrid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + opFlags.hashCode()
        result = 31 * result + coordGrid.hashCode()
        return result
    }

    override fun toString(): String =
        "ObjEnabledOpsSpecific(" +
            "id=$id, " +
            "opFlags=$opFlags, " +
            "coordGrid=$coordGrid" +
            ")"
}
