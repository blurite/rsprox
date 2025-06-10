package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Proj anim specific packets are used to send a projectile for a specific user,
 * without anyone else in the world seeing it.
 * Unlike the previous specific projectiles and other zone packets, this one writes absolute coordinates,
 * not zone-local ones.
 * Additionally, unlike the older packets, this one does not implicitly multiply heights by 4.
 * Servers must do it themselves to end up with the same output.

 * @property id the id of the spotanim that is this projectile
 * @property startHeight the height of the projectile as it begins flying
 * @property endHeight the height of the projectile as it finishes flying
 * @property startTime the start time in client cycles (20ms/cc) until the
 * projectile begins moving
 * @property endTime the end time in client cycles (20ms/cc) until the
 * projectile arrives at its destination
 * @property angle the angle that the projectile takes during its flight
 * @property progress the fine coord progress that the projectile
 * has made before it begins flying. If the value is 0, the projectile begins flying
 * at the defined start coordinate. For every 128 units of value, the projectile
 * is moved 1 game square towards the end position. Interpolate between 0-128 for
 * units smaller than 1 game square.
 * This is commonly set to 128 to make a projectile appear as if it's flying
 * straight down, as the projectile will not render if its defined start and
 * end coords are equal. So, in order to avoid that, one solution is to put the
 * end coordinate 1 game square away from the start in a cardinal direction,
 * and set the value of this property to 128 - ensuring that the projectile
 * will appear to fly completely vertically, with no horizontal movement whatsoever.
 * In the event inspector, this property is called 'distanceOffset'.
 * @property sourceIndex the index of the pathing entity from whom the projectile is shot.
 * If the value is 0, the projectile will not be locked to any source entity.
 * If the source avatar is a player, add 0x10000 to the real index value (0-2048).
 * If the source avatar is a NPC, set the index as it is.
 * @property targetIndex the index of the pathing entity at whom the projectile is shot.
 * If the value is 0, the projectile will not be locked to any target entity.
 * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
 * If the target avatar is a NPC, set the index as it is.
 * @property start the starting coordinate from which the projectile begins flying
 * @property end the ending coordinate to which the projectile will fly when not locked onto
 * a target.
 */
@Suppress("DuplicatedCode")
public class ProjAnimSpecificV4 private constructor(
    private val _id: UShort,
    private val _startHeight: UShort,
    private val _endHeight: UShort,
    private val _startTime: UShort,
    private val _endTime: UShort,
    private val _angle: UByte,
    private val _progress: UShort,
    public val sourceIndex: Int,
    public val targetIndex: Int,
    public val start: CoordGrid,
    public val end: CoordGrid,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        startHeight: Int,
        endHeight: Int,
        startTime: Int,
        endTime: Int,
        angle: Int,
        progress: Int,
        start: CoordGrid,
        sourceIndex: Int,
        end: CoordGrid,
        targetIndex: Int,
    ) : this(
        id.toUShort(),
        startHeight.toUShort(),
        endHeight.toUShort(),
        startTime.toUShort(),
        endTime.toUShort(),
        angle.toUByte(),
        progress.toUShort(),
        sourceIndex,
        targetIndex,
        start,
        end,
    )

    public val id: Int
        get() = _id.toInt()
    public val startHeight: Int
        get() = _startHeight.toInt()
    public val endHeight: Int
        get() = _endHeight.toInt()
    public val startTime: Int
        get() = _startTime.toInt()
    public val endTime: Int
        get() = _endTime.toInt()
    public val angle: Int
        get() = _angle.toInt()
    public val progress: Int
        get() = _progress.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjAnimSpecificV4

        if (_id != other._id) return false
        if (_startHeight != other._startHeight) return false
        if (_endHeight != other._endHeight) return false
        if (_startTime != other._startTime) return false
        if (_endTime != other._endTime) return false
        if (_angle != other._angle) return false
        if (_progress != other._progress) return false
        if (sourceIndex != other.sourceIndex) return false
        if (targetIndex != other.targetIndex) return false
        if (start != other.start) return false
        if (end != other.end) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _startHeight.hashCode()
        result = 31 * result + _endHeight.hashCode()
        result = 31 * result + _startTime.hashCode()
        result = 31 * result + _endTime.hashCode()
        result = 31 * result + _angle.hashCode()
        result = 31 * result + _progress.hashCode()
        result = 31 * result + sourceIndex
        result = 31 * result + targetIndex
        result = 31 * result + start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }

    override fun toString(): String {
        return "ProjAnimSpecificV3(" +
            "id=$id, " +
            "startHeight=$startHeight, " +
            "endHeight=$endHeight, " +
            "startTime=$startTime, " +
            "endTime=$endTime, " +
            "angle=$angle, " +
            "progress=$progress, " +
            "start=$start, " +
            "sourceIndex=$sourceIndex, " +
            "end=$end, " +
            "targetIndex=$targetIndex, " +
            ")"
    }
}
