package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Obj customise is a packet that allows the server to modify an item on the ground, by either changing
 * the model, the colours and the textures of it.
 * @property id the id of the obj to update
 * @property quantity the quantity of the obj to update
 * @property model the model id to assign to this obj
 * @property recolIndex the index of the colour to override
 * @property recol the colour value to assign at the [recolIndex] index
 * @property retexIndex the index of the texture to override
 * @property retex the texture value to assign at the [retexIndex] index
 * @property coordGrid the absolute coordinate at which the obj is modified.
 */
public class ObjCustomiseSpecific private constructor(
    private val _id: UShort,
    public val quantity: Int,
    private val _model: UShort,
    private val _recolIndex: Short,
    private val _recol: Short,
    private val _retexIndex: Short,
    private val _retex: Short,
    public val coordGrid: CoordGrid,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        quantity: Int,
        model: Int,
        recolIndex: Int,
        recol: Int,
        retexIndex: Int,
        retex: Int,
        coordGrid: CoordGrid,
    ) : this(
        id.toUShort(),
        quantity,
        model.toUShort(),
        recolIndex.toShort(),
        recol.toShort(),
        retexIndex.toShort(),
        retex.toShort(),
        coordGrid,
    )

    public val id: Int
        get() = _id.toInt()
    public val model: Int
        get() = _model.toIntOrMinusOne()
    public val recolIndex: Int
        get() = _recolIndex.toInt()
    public val recol: Int
        get() = _recol.toInt()
    public val retexIndex: Int
        get() = _retexIndex.toInt()
    public val retex: Int
        get() = _retex.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjCustomiseSpecific) return false

        if (_id != other._id) return false
        if (quantity != other.quantity) return false
        if (_model != other._model) return false
        if (_recolIndex != other._recolIndex) return false
        if (_recol != other._recol) return false
        if (_retexIndex != other._retexIndex) return false
        if (_retex != other._retex) return false
        if (coordGrid != other.coordGrid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + quantity
        result = 31 * result + _model.hashCode()
        result = 31 * result + _recolIndex
        result = 31 * result + _recol
        result = 31 * result + _retexIndex
        result = 31 * result + _retex
        result = 31 * result + coordGrid.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjCustomiseSpecific(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "model=$model, " +
            "recolIndex=$recolIndex, " +
            "recol=$recol, " +
            "retexIndex=$retexIndex, " +
            "retex=$retex, " +
            "coordGrid=$coordGrid" +
            ")"
    }
}
