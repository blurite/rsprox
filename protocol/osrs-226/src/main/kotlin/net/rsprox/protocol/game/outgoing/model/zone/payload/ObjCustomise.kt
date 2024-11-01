package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone

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
 * @property xInZone the x coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 */
public class ObjCustomise private constructor(
    private val _id: UShort,
    public val quantity: Int,
    private val _model: UShort,
    private val _recolIndex: Short,
    private val _recol: Short,
    private val _retexIndex: Short,
    private val _retex: Short,
    private val coordInZone: CoordInZone,
) : IncomingZoneProt {
    public constructor(
        id: Int,
        quantity: Int,
        model: Int,
        recolIndex: Int,
        recol: Int,
        retexIndex: Int,
        retex: Int,
        xInZone: Int,
        zInZone: Int,
    ) : this(
        id.toUShort(),
        quantity,
        model.toUShort(),
        recolIndex.toShort(),
        recol.toShort(),
        retexIndex.toShort(),
        retex.toShort(),
        CoordInZone(xInZone, zInZone),
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
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    override val protId: Int = OldSchoolZoneProt.OBJ_CUSTOMISE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjCustomise) return false

        if (_id != other._id) return false
        if (quantity != other.quantity) return false
        if (_model != other._model) return false
        if (_recolIndex != other._recolIndex) return false
        if (_recol != other._recol) return false
        if (_retexIndex != other._retexIndex) return false
        if (_retex != other._retex) return false
        if (coordInZone != other.coordInZone) return false

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
        result = 31 * result + coordInZone.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjCustomise(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "model=$model, " +
            "recolIndex=$recolIndex, " +
            "recol=$recol, " +
            "retexIndex=$retexIndex, " +
            "retex=$retex, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
    }
}
