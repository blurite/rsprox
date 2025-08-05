package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Obj del packets are used to delete an existing obj from the build area,
 * assuming it exists in the first place.
 * @property id the id of the obj to delete. Note that the client does bitwise-and
 * on the id to cap it to the lowest 15 bits, meaning the maximum id that can be
 * transmitted is 32767.
 * @property quantity the quantity of the obj to be deleted. If there is no obj
 * with this quantity, nothing will be deleted.
 * @property coordGrid the absolute coordinate at which the obj is deleted.
 */
public class ObjDelSpecific private constructor(
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
        if (javaClass != other?.javaClass) return false

        other as ObjDelSpecific

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

    override fun toString(): String =
        "ObjDelSpecific(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "coordGrid=$coordGrid" +
            ")"
}
