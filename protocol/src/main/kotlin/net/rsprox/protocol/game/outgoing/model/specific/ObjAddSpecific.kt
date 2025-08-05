package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.util.OpFlags

/**
 * Obj add packets are used to spawn an obj on the ground.
 *
 * Ownership table:
 * ```
 * | Id | Ownership Type |
 * |----|:--------------:|
 * | 0  |      None      |
 * | 1  |   Self Player  |
 * | 2  |  Other Player  |
 * | 3  |  Group Ironman |
 * ```
 *
 * @property id the id of the obj config
 * @property quantity the quantity of the obj to be spawned
 * @property coordGrid the absolute coordinate at which the obj is added.
 * @property opFlags the right-click options enabled on this obj.
 * Use the [net.rsprot.protocol.game.outgoing.util.OpFlags] helper object to create these
 * bitpacked values which can be passed into it.
 * @property timeUntilPublic how many game cycles until the obj turns public.
 * This property is only used on the C++-based clients.
 * @property timeUntilDespawn how many game cycles until the obj disappears.
 * This property is only used on the C++-based clients.
 * @property ownershipType the type of ownership of this obj (see table above).
 * This property is only used on the C++-based clients.
 * @property neverBecomesPublic whether the item turns public in the future.
 * This property is only used on the c++-based clients.
 */
@Suppress("DuplicatedCode")
public class ObjAddSpecific private constructor(
    private val _id: UShort,
    public val quantity: Int,
    public val coordGrid: CoordGrid,
    public val opFlags: OpFlags,
    private val _timeUntilPublic: UShort,
    private val _timeUntilDespawn: UShort,
    private val _ownershipType: UByte,
    public val neverBecomesPublic: Boolean,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        quantity: Int,
        coordGrid: CoordGrid,
        opFlags: OpFlags,
        timeUntilPublic: Int,
        timeUntilDespawn: Int,
        ownershipType: Int,
        neverBecomesPublic: Boolean,
    ) : this(
        id.toUShort(),
        quantity,
        coordGrid,
        opFlags,
        timeUntilPublic.toUShort(),
        timeUntilDespawn.toUShort(),
        ownershipType.toUByte(),
        neverBecomesPublic,
    )

    public val id: Int
        get() = _id.toInt()
    public val timeUntilPublic: Int
        get() = _timeUntilPublic.toInt()
    public val timeUntilDespawn: Int
        get() = _timeUntilDespawn.toInt()
    public val ownershipType: Int
        get() = _ownershipType.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjAddSpecific

        if (_id != other._id) return false
        if (quantity != other.quantity) return false
        if (coordGrid != other.coordGrid) return false
        if (opFlags != other.opFlags) return false
        if (_timeUntilPublic != other._timeUntilPublic) return false
        if (_timeUntilDespawn != other._timeUntilDespawn) return false
        if (_ownershipType != other._ownershipType) return false
        if (neverBecomesPublic != other.neverBecomesPublic) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + quantity
        result = 31 * result + coordGrid.hashCode()
        result = 31 * result + opFlags.hashCode()
        result = 31 * result + _timeUntilPublic.hashCode()
        result = 31 * result + _timeUntilDespawn.hashCode()
        result = 31 * result + _ownershipType.hashCode()
        result = 31 * result + neverBecomesPublic.hashCode()
        return result
    }

    override fun toString(): String =
        "ObjAddSpecific(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "coordGrid=$coordGrid, " +
            "opFlags=$opFlags, " +
            "timeUntilPublic=$timeUntilPublic, " +
            "timeUntilDespawn=$timeUntilDespawn, " +
            "ownershipType=$ownershipType" +
            ")"
}
