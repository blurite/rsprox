package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.ZoneProt
import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone

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
 * @property xInZone the x coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property opFlags the right-click options enabled on this obj.
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
public class ObjAdd private constructor(
    private val _id: UShort,
    public val quantity: Int,
    private val coordInZone: CoordInZone,
    public val opFlags: OpFlags,
    private val _timeUntilPublic: UShort,
    private val _timeUntilDespawn: UShort,
    private val _ownershipType: UByte,
    public val neverBecomesPublic: Boolean,
) : ZoneProt {
    public constructor(
        id: Int,
        quantity: Int,
        xInZone: Int,
        zInZone: Int,
        opFlags: OpFlags,
        timeUntilPublic: Int,
        timeUntilDespawn: Int,
        ownershipType: Int,
        neverBecomesPublic: Boolean,
    ) : this(
        id.toUShort(),
        quantity,
        CoordInZone(xInZone, zInZone),
        opFlags,
        timeUntilPublic.toUShort(),
        timeUntilDespawn.toUShort(),
        ownershipType.toUByte(),
        neverBecomesPublic,
    )

    /**
     * A helper constructor for the JVM-based clients, as these clients
     * do not utilize the [timeUntilPublic], [timeUntilDespawn], [ownershipType] and
     * [neverBecomesPublic] properties.
     */
    public constructor(
        id: Int,
        quantity: Int,
        xInZone: Int,
        zInZone: Int,
        opFlags: OpFlags,
    ) : this(
        id,
        quantity,
        xInZone,
        zInZone,
        opFlags,
        timeUntilPublic = 0,
        timeUntilDespawn = 0,
        ownershipType = 0,
        neverBecomesPublic = false,
    )

    public val id: Int
        get() = _id.toInt()
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone
    public val timeUntilPublic: Int
        get() = _timeUntilPublic.toInt()
    public val timeUntilDespawn: Int
        get() = _timeUntilDespawn.toInt()
    public val ownershipType: Int
        get() = _ownershipType.toInt()

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
    override val protId: Int = OldSchoolZoneProt.OBJ_ADD

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjAdd

        if (_id != other._id) return false
        if (quantity != other.quantity) return false
        if (coordInZone != other.coordInZone) return false
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
        result = 31 * result + coordInZone.hashCode()
        result = 31 * result + opFlags.hashCode()
        result = 31 * result + _timeUntilPublic.hashCode()
        result = 31 * result + _timeUntilDespawn.hashCode()
        result = 31 * result + _ownershipType.hashCode()
        result = 31 * result + neverBecomesPublic.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjAdd(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone, " +
            "opFlags=$opFlags, " +
            "timeUntilPublic=$timeUntilPublic, " +
            "timeUntilDespawn=$timeUntilDespawn, " +
            "ownershipType=$ownershipType" +
            ")"
    }
}
