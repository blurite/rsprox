package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Obj uncustomise resets any customisations done to an obj via the [ObjCustomise] packet.
 * @property id the id of the obj to update
 * @property quantity the quantity of the obj to update
 * @property coordGrid the absolute coordinate at which the obj is modified.
 */
public class ObjUncustomiseSpecific private constructor(
    private val _id: UShort,
    public val quantity: Int,
    public val coordGrid: CoordGrid,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        quantity: Int,
        coordGrid: CoordGrid,
    ) : this(
        id.toUShort(),
        quantity,
        coordGrid,
    )

    public val id: Int
        get() = _id.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjUncustomiseSpecific) return false

        if (_id != other._id) return false
        if (quantity != other.quantity) return false
        if (coordGrid != other.coordGrid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + quantity
        result = 31 * result + coordGrid.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjCustomiseSpecific(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "coordGrid=$coordGrid" +
            ")"
    }
}
