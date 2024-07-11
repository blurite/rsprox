package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties

/**
 * Loc merge packets are used to merge a given loc's model with the player's
 * own model, preventing any visual clipping problems in the process.
 * This is commonly done with obstacle pipes in agility courses, as
 * the player model will otherwise render through the pipes.
 *
 * The merge will cover a rectangle defined by the [minX], [minZ], [maxX] and [maxZ]
 * properties, relative to the player who is being merged. It should be noted
 * that the client adds an extra 1 to the total width/height values here,
 * so having all these properties at zero would still create a single
 * tile square to be merged.
 *
 * @property index the index of the player who is being merged
 * @property id the id of the loc that is being merged with the player
 * @property xInZone the x coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property shape the shape of the loc, a value of 0 to 22 (inclusive) is expected.
 * @property rotation the rotation of the loc, a value of 0 to 3 (inclusive) is expected.
 * @property start the delay until the loc merging begins, in client cycles (20ms/cc).
 * @property end the client cycle (20ms/cc) at which the merging ends.
 * @property minX the min x coordinate at which the merge occurs (see explanation above)
 * @property minZ the min z coordinate at which the merge occurs (see explanation above)
 * @property maxX the max x coordinate at which the merge occurs (see explanation above)
 * @property maxZ the max z coordinate at which the merge occurs (see explanation above)
 */
@Suppress("DuplicatedCode")
public class LocMerge private constructor(
    private val _index: UShort,
    private val _id: UShort,
    private val coordInZone: CoordInZone,
    private val locProperties: LocProperties,
    private val _start: UShort,
    private val _end: UShort,
    private val _minX: Byte,
    private val _minZ: Byte,
    private val _maxX: Byte,
    private val _maxZ: Byte,
) : IncomingZoneProt {
    public constructor(
        index: Int,
        id: Int,
        xInZone: Int,
        zInZone: Int,
        shape: Int,
        rotation: Int,
        start: Int,
        end: Int,
        minX: Int,
        minZ: Int,
        maxX: Int,
        maxZ: Int,
    ) : this(
        index.toUShort(),
        id.toUShort(),
        CoordInZone(xInZone, zInZone),
        LocProperties(shape, rotation),
        start.toUShort(),
        end.toUShort(),
        minX.toByte(),
        minZ.toByte(),
        maxX.toByte(),
        maxZ.toByte(),
    )

    internal constructor(
        index: Int,
        id: Int,
        coordInZone: CoordInZone,
        locProperties: LocProperties,
        start: Int,
        end: Int,
        minX: Int,
        minZ: Int,
        maxX: Int,
        maxZ: Int,
    ) : this(
        index.toUShort(),
        id.toUShort(),
        coordInZone,
        locProperties,
        start.toUShort(),
        end.toUShort(),
        minX.toByte(),
        minZ.toByte(),
        maxX.toByte(),
        maxZ.toByte(),
    )

    public val index: Int
        get() = _index.toInt()
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
    public val start: Int
        get() = _start.toInt()
    public val end: Int
        get() = _end.toInt()
    public val minX: Int
        get() = _minX.toInt()
    public val minZ: Int
        get() = _minZ.toInt()
    public val maxX: Int
        get() = _maxX.toInt()
    public val maxZ: Int
        get() = _maxZ.toInt()

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    public val locPropertiesPacked: Int
        get() = locProperties.packed.toInt()

    override val protId: Int = OldSchoolZoneProt.LOC_MERGE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocMerge

        if (_index != other._index) return false
        if (_id != other._id) return false
        if (coordInZone != other.coordInZone) return false
        if (locProperties != other.locProperties) return false
        if (_start != other._start) return false
        if (_end != other._end) return false
        if (_minX != other._minX) return false
        if (_minZ != other._minZ) return false
        if (_maxX != other._maxX) return false
        if (_maxZ != other._maxZ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _index.hashCode()
        result = 31 * result + _id.hashCode()
        result = 31 * result + coordInZone.hashCode()
        result = 31 * result + locProperties.hashCode()
        result = 31 * result + _start.hashCode()
        result = 31 * result + _end.hashCode()
        result = 31 * result + _minX.hashCode()
        result = 31 * result + _minZ.hashCode()
        result = 31 * result + _maxX.hashCode()
        result = 31 * result + _maxZ.hashCode()
        return result
    }

    override fun toString(): String {
        return "LocMerge(" +
            "index=$index, " +
            "id=$id, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone, " +
            "shape=$shape, " +
            "rotation=$rotation, " +
            "start=$start, " +
            "end=$end, " +
            "minX=$minX, " +
            "minZ=$minZ, " +
            "maxX=$maxX, " +
            "maxZ=$maxZ" +
            ")"
    }
}
