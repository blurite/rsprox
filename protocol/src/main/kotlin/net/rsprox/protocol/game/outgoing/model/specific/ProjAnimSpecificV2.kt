package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea

/**
 * Proj anim specific packets are used to send a projectile for a specific user,
 * without anyone else in the world seeing it.
 * Unlike the regular [net.rsprot.protocol.game.outgoing.zone.payload.MapProjAnim]
 * zone packet, this packet does not support transmitting the source index.
 *
 * While it is possible to compress this message further as the [targetIndex]
 * only needs to be a 24-bit integer, the entire payload of this packet
 * sums to just 19 bytes, which is 1 less than the 8-byte padding that will
 * be performed by the JVM anyway - so there are no benefits in doing so.
 *
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
 * @property targetIndex the index of the pathing entity at whom the projectile is shot.
 * If the value is 0, the projectile will not be locked to any target entity.
 * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
 * If the target avatar is a NPC, set the index as it is.
 * @property zoneX the x coordinate of the zone's south-western corner in the
 * build area.
 * @property xInZone the start x coordinate of the projectile within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zoneZ the z coordinate of the zone's south-western corner in the
 * build area.
 * @property zInZone the start z coordinate of the projectile within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property deltaX the x coordinate delta that the projectile will move to
 * relative to the starting position.
 * @property deltaZ the z coordinate delta that the projectile will move to
 * relative to the starting position.
 *
 * It should be noted that the [zoneX] and [zoneZ] coordinates are relative
 * to the build area in their absolute form, not in their shifted zone form.
 * If the player is at an absolute coordinate of 50, 40 within the build area(104x104),
 * the expected coordinates to transmit here would be 48, 40, as that would
 * point to the south-western corner of the zone in which the player is standing in.
 * The client will add up the respective [zoneX] + [xInZone] properties together,
 * along with [zoneZ] + [zInZone] to re-create the effects of a normal zone packet.
 */
@Suppress("DuplicatedCode")
public class ProjAnimSpecificV2 private constructor(
    private val _id: UShort,
    private val _startHeight: UByte,
    private val _endHeight: UByte,
    private val _startTime: UShort,
    private val _endTime: UShort,
    private val _angle: UByte,
    private val _progress: UShort,
    public val targetIndex: Int,
    public val coordInBuildArea: CoordInBuildArea,
    private val _deltaX: Byte,
    private val _deltaZ: Byte,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        startHeight: Int,
        endHeight: Int,
        startTime: Int,
        endTime: Int,
        angle: Int,
        progress: Int,
        targetIndex: Int,
        zoneX: Int,
        xInZone: Int,
        zoneZ: Int,
        zInZone: Int,
        deltaX: Int,
        deltaZ: Int,
    ) : this(
        id.toUShort(),
        startHeight.toUByte(),
        endHeight.toUByte(),
        startTime.toUShort(),
        endTime.toUShort(),
        angle.toUByte(),
        progress.toUShort(),
        targetIndex,
        CoordInBuildArea(
            zoneX,
            xInZone,
            zoneZ,
            zInZone,
        ),
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
        targetIndex: Int,
        xInBuildArea: Int,
        zInBuildArea: Int,
        deltaX: Int,
        deltaZ: Int,
    ) : this(
        id.toUShort(),
        startHeight.toUByte(),
        endHeight.toUByte(),
        startTime.toUShort(),
        endTime.toUShort(),
        angle.toUByte(),
        progress.toUShort(),
        targetIndex,
        CoordInBuildArea(
            xInBuildArea,
            zInBuildArea,
        ),
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
        targetIndex: Int,
        coordInBuildArea: CoordInBuildArea,
        deltaX: Int,
        deltaZ: Int,
    ) : this(
        id.toUShort(),
        startHeight.toUByte(),
        endHeight.toUByte(),
        startTime.toUShort(),
        endTime.toUShort(),
        angle.toUByte(),
        progress.toUShort(),
        targetIndex,
        coordInBuildArea,
        deltaX.toByte(),
        deltaZ.toByte(),
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
    public val zoneX: Int
        get() = coordInBuildArea.zoneX
    public val xInZone: Int
        get() = coordInBuildArea.xInZone
    public val zoneZ: Int
        get() = coordInBuildArea.zoneZ
    public val zInZone: Int
        get() = coordInBuildArea.zInZone
    public val deltaX: Int
        get() = _deltaX.toInt()
    public val deltaZ: Int
        get() = _deltaZ.toInt()

    public val coordInBuildAreaPacked: Int
        get() = coordInBuildArea.packedMedium

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjAnimSpecificV2

        if (_id != other._id) return false
        if (_startHeight != other._startHeight) return false
        if (_endHeight != other._endHeight) return false
        if (_startTime != other._startTime) return false
        if (_endTime != other._endTime) return false
        if (_angle != other._angle) return false
        if (_progress != other._progress) return false
        if (targetIndex != other.targetIndex) return false
        if (coordInBuildArea != other.coordInBuildArea) return false
        if (_deltaX != other._deltaX) return false
        if (_deltaZ != other._deltaZ) return false

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
        result = 31 * result + targetIndex
        result = 31 * result + coordInBuildArea.hashCode()
        result = 31 * result + _deltaX
        result = 31 * result + _deltaZ
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
            "targetIndex=$targetIndex, " +
            "zoneX=$zoneX, " +
            "xInZone=$xInZone, " +
            "zoneZ=$zoneZ, " +
            "zInZone=$zInZone, " +
            "deltaX=$deltaX, " +
            "deltaZ=$deltaZ" +
            ")"
    }
}
