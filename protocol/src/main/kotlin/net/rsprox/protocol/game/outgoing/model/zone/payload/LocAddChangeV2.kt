package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties

/**
 * Loc add-change v2 packet is used to either add or change a loc in the world.
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
 * @property ops a map of mini menu ops to override the defaults with.
 * If the map is null or empty, the ops will not be overridden and the ones provided in the
 * respective cache config will be used. If the map has entries, **all** the cache ops are
 * ignored and the provided map is used. Note that only ops 1-5 will actually be used, any
 * other values get ignored by the client. As such, if a map is provided that has no keys
 * of value 1-5, all the ops will simply be hidden.
 */
public class LocAddChangeV2 private constructor(
    private val _id: UShort,
    private val coordInZone: CoordInZone,
    private val locProperties: LocProperties,
    public val opFlags: OpFlags,
    public val ops: Map<Byte, String>?,
) : IncomingZoneProt {
    public constructor(
        id: Int,
        xInZone: Int,
        zInZone: Int,
        shape: Int,
        rotation: Int,
        opFlags: OpFlags,
        ops: Map<Byte, String>?,
    ) : this(
        id.toUShort(),
        CoordInZone(xInZone, zInZone),
        LocProperties(shape, rotation),
        opFlags,
        ops,
    )

    public constructor(
        id: Int,
        coordInZone: CoordInZone,
        locProperties: LocProperties,
        opFlags: OpFlags,
        ops: Map<Byte, String>?,
    ) : this(
        id.toUShort(),
        coordInZone,
        locProperties,
        opFlags,
        ops,
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

    override val protId: Int = OldSchoolZoneProt.LOC_ADD_CHANGE_V2

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocAddChangeV2

        if (_id != other._id) return false
        if (coordInZone != other.coordInZone) return false
        if (locProperties != other.locProperties) return false
        if (opFlags != other.opFlags) return false
        if (ops != other.ops) return false
        if (protId != other.protId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + coordInZone.hashCode()
        result = 31 * result + locProperties.hashCode()
        result = 31 * result + opFlags.hashCode()
        result = 31 * result + (ops?.hashCode() ?: 0)
        result = 31 * result + protId
        return result
    }

    override fun toString(): String {
        return "LocAddChangeV2(" +
            "id=$id, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone, " +
            "shape=$shape, " +
            "rotation=$rotation" +
            "opFlags=$opFlags, " +
            "ops=$ops, " +
            ")"
    }
}
