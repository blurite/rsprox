package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone

/**
 * Map projectile anim packets are sent to render projectiles
 * from one coord to another.
 * @property id the id of the spotanim that is this projectile
 * @property startHeight the height of the projectile as it begins flying
 * @property endHeight the height of the projectile as it finishes flying
 * @property startTime the start time in client cycles (20ms/cc) until the
 * projectile begins moving
 * @property endTime the end time in client cycles (20ms/cc) until the
 * projectile arrives at its destination
 * @property angle the angle that the projectile takes during its flight
 * @property progress the fine coord distance offset that the projectile
 * begins flying at. If the value is 0, the projectile begins flying
 * at the defined start coordinate. For every 128 units of value, the projectile
 * is moved 1 game square towards the end position. Interpolate between 0-128 for
 * units smaller than 1 game square.
 * This is commonly set to 128 to make a projectile appear as if it's flying
 * straight down, as the projectile will not render if its defined start and
 * end coords are equal. So, in order to avoid that, one solution is to put the
 * end coordinate 1 game square away from the start in a cardinal direction,
 * and set the value of this property to 128 - ensuring that the projectile
 * will appear to fly completely vertically, with no horizontal movement whatsoever.
 * @property sourceIndex the index of the pathing entity from whom the projectile comes.
 * If the value is 0, the projectile will not be locked to any source entity.
 * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
 * If the target avatar is a NPC, set the index as it is.
 * @property targetIndex the index of the pathing entity at whom the projectile is shot.
 * If the value is 0, the projectile will not be locked to any target entity.
 * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
 * If the target avatar is a NPC, set the index as it is.
 * @property xInZone the start x coordinate of the projectile within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the start z coordinate of the projectile within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property deltaX the x coordinate delta that the projectile will move to
 * relative to the starting position.
 * @property deltaZ the z coordinate delta that the projectile will move to
 * relative to the starting position.
 */
@Suppress("DuplicatedCode")
public class MapProjAnim private constructor(
    private val _id: UShort,
    private val _startTime: UShort,
    private val _endTime: UShort,
    private val _angle: UByte,
    private val _progress: UShort,
    private val compressedInfo: CompressedMapProjAnimInfo,
    private val coordInZone: CoordInZone,
    private val _deltaX: Byte,
    private val _deltaZ: Byte,
) : IncomingZoneProt {
    public constructor(
        id: Int,
        startHeight: Int,
        endHeight: Int,
        startTime: Int,
        endTime: Int,
        angle: Int,
        progress: Int,
        sourceIndex: Int,
        targetIndex: Int,
        xInZone: Int,
        zInZone: Int,
        deltaX: Int,
        deltaZ: Int,
    ) : this(
        id.toUShort(),
        startTime.toUShort(),
        endTime.toUShort(),
        angle.toUByte(),
        progress.toUShort(),
        CompressedMapProjAnimInfo(
            sourceIndex,
            targetIndex,
            startHeight.toUByte(),
            endHeight.toUByte(),
        ),
        CoordInZone(xInZone, zInZone),
        deltaX.toByte(),
        deltaZ.toByte(),
    )

    public constructor(
        id: Int,
        startHeight: Int,
        endHeight: Int,
        startTime: Int,
        endTime: Int,
        angle: Int,
        progress: Int,
        sourceIndex: Int,
        targetIndex: Int,
        coordInZone: CoordInZone,
        deltaX: Int,
        deltaZ: Int,
    ) : this(
        id.toUShort(),
        startTime.toUShort(),
        endTime.toUShort(),
        angle.toUByte(),
        progress.toUShort(),
        CompressedMapProjAnimInfo(
            sourceIndex,
            targetIndex,
            startHeight.toUByte(),
            endHeight.toUByte(),
        ),
        coordInZone,
        deltaX.toByte(),
        deltaZ.toByte(),
    )

    public val id: Int
        get() = _id.toInt()
    public val startHeight: Int
        get() = compressedInfo.startHeight
    public val endHeight: Int
        get() = compressedInfo.endHeight
    public val startTime: Int
        get() = _startTime.toInt()
    public val endTime: Int
        get() = _endTime.toInt()
    public val angle: Int
        get() = _angle.toInt()
    public val progress: Int
        get() = _progress.toInt()
    public val sourceIndex: Int
        get() = compressedInfo.sourceIndex
    public val targetIndex: Int
        get() = compressedInfo.targetIndex
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone
    public val deltaX: Int
        get() = _deltaX.toInt()
    public val deltaZ: Int
        get() = _deltaZ.toInt()

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    override val protId: Int = OldSchoolZoneProt.MAP_PROJANIM

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapProjAnim

        if (_id != other._id) return false
        if (_startTime != other._startTime) return false
        if (_endTime != other._endTime) return false
        if (_angle != other._angle) return false
        if (_progress != other._progress) return false
        if (compressedInfo != other.compressedInfo) return false
        if (coordInZone != other.coordInZone) return false
        if (_deltaX != other._deltaX) return false
        if (_deltaZ != other._deltaZ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _startTime.hashCode()
        result = 31 * result + _endTime.hashCode()
        result = 31 * result + _angle.hashCode()
        result = 31 * result + _progress.hashCode()
        result = 31 * result + compressedInfo.hashCode()
        result = 31 * result + coordInZone.hashCode()
        result = 31 * result + _deltaX
        result = 31 * result + _deltaZ
        return result
    }

    override fun toString(): String {
        return "MapProjAnim(" +
            "id=$id, " +
            "startHeight=$startHeight, " +
            "endHeight=$endHeight, " +
            "startTime=$startTime, " +
            "endTime=$endTime, " +
            "angle=$angle, " +
            "progress=$progress, " +
            "sourceIndex=$sourceIndex, " +
            "targetIndex=$targetIndex, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone, " +
            "deltaX=$deltaX, " +
            "deltaZ=$deltaZ" +
            ")"
    }

    /**
     * A value class to compress several properties into one.
     * This is primarily done so the entire class comes to a sum of 20 bytes.
     * The [sourceIndex] and [targetIndex] properties are 24-bit integers, for
     * which there are no backing types in the JVM. Treating them as 32-bit
     * integers would push the total sum of all the payload to 22 bytes, which,
     * due to memory alignment would cause the entire thing to take 28 bytes
     * instead of the usual 20.
     */
    @JvmInline
    private value class CompressedMapProjAnimInfo private constructor(
        private val packed: Long,
    ) {
        constructor(
            sourceIndex: Int,
            targetIndex: Int,
            startHeight: UByte,
            endHeight: UByte,
        ) : this(
            (sourceIndex and 0xFFFFFF)
                .toLong()
                .or((targetIndex and 0xFFFFFF).toLong() shl 24)
                .or((startHeight.toLong() and 0xFF) shl 48)
                .or((endHeight.toLong() and 0xFF) shl 56),
        )

        val sourceIndex: Int
            get() = signMedium((packed and 0xFFFFFF).toInt())
        val targetIndex: Int
            get() = signMedium((packed ushr 24 and 0xFFFFFF).toInt())
        val startHeight: Int
            get() = (packed ushr 48 and 0xFF).toInt()
        val endHeight: Int
            get() = (packed ushr 56 and 0xFF).toInt()

        private fun signMedium(num: Int): Int {
            return if (num > 8388607) {
                num - 16777216
            } else {
                num
            }
        }

        override fun toString(): String {
            return "MapProjAnimInfo(" +
                "sourceIndex=$sourceIndex, " +
                "targetIndex=$targetIndex, " +
                "startHeight=$startHeight, " +
                "endHeight=$endHeight" +
                ")"
        }
    }
}
