package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea

/**
 * Cam move to packet is used to move the position of the camera
 * to a specific coordinate within the current build area.
 * It is important to note that if this is sent together with
 * a map reload, whether this packet comes before or after the
 * map reload makes a difference - as the build area itself changes.
 *
 * @property destinationXInBuildArea the dest x coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property destinationZInBuildArea the dest z coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property height the height of the camera
 * @property speed the constant speed at which the camera moves
 * to the new coordinate
 * @property acceleration the speed increase as the camera moves
 * towards the end coordinate.
 */
public class CamMoveToV1 private constructor(
    private val destinationCoordInBuildArea: CoordInBuildArea,
    private val _height: UShort,
    private val _speed: UByte,
    private val _acceleration: UByte,
) : IncomingServerGameMessage {
    public constructor(
        xInBuildArea: Int,
        zInBuildArea: Int,
        height: Int,
        speed: Int,
        acceleration: Int,
    ) : this(
        CoordInBuildArea(xInBuildArea, zInBuildArea),
        height.toUShort(),
        speed.toUByte(),
        acceleration.toUByte(),
    )

    public val destinationXInBuildArea: Int
        get() = destinationCoordInBuildArea.xInBuildArea
    public val destinationZInBuildArea: Int
        get() = destinationCoordInBuildArea.zInBuildArea
    public val height: Int
        get() = _height.toInt()
    public val speed: Int
        get() = _speed.toInt()
    public val acceleration: Int
        get() = _acceleration.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamMoveToV1

        if (destinationCoordInBuildArea != other.destinationCoordInBuildArea) return false
        if (_height != other._height) return false
        if (_speed != other._speed) return false
        if (_acceleration != other._acceleration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = destinationCoordInBuildArea.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + _speed.hashCode()
        result = 31 * result + _acceleration.hashCode()
        return result
    }

    override fun toString(): String {
        return "CamMoveToV1(" +
            "destinationXInBuildArea=$destinationXInBuildArea, " +
            "destinationZInBuildArea=$destinationZInBuildArea, " +
            "height=$height, " +
            "speed=$speed, " +
            "acceleration=$acceleration" +
            ")"
    }
}
