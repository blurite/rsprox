package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.camera.util.CameraEaseFunction
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea

/**
 * Camera move to eased circular packet is used to move camera
 * to a new coordinate with finer control behind it.
 * This packet differs from [CamMoveToCycles] in that it will first
 * move through a center coordinate before going towards the destination,
 * creating a `)`-shape movement. An example image of this can be seen
 * [here](https://media.z-kris.com/2024/04/cam%20move%20eased%20circular.png)
 *
 * @property centerXInBuildArea the center x coordinate within the build area,
 * in range of 0 to 103 (inclusive). This marks the middle point between the
 * camera movement through which the camera has to go.
 * @property centerZInBuildArea the center z coordinate within the build area,
 * in range of 0 to 103 (inclusive). This marks the middle point between the
 * camera movement through which the camera has to go.
 * @property destinationXInBuildArea the dest x coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property destinationZInBuildArea the dest z coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property height the height of the camera once it arrives at the destination
 * @property duration the duration of the movement in client cycles (20ms/cc)
 * @property maintainFixedAltitude whether the camera moves along the terrain,
 * moving up and down according to bumps in the terrain.
 * If false, the camera will move in a straight line from the starting position
 * towards the end position, ignoring any changes in the terrain.
 * @property function the camera easing function, allowing for finer
 * control over the way it moves from the start coordinate to the end.
 */
@Suppress("DuplicatedCode")
public class CamMoveToEasedCircular private constructor(
    private val centerCoordInBuildArea: CoordInBuildArea,
    private val destinationCoordInBuildArea: CoordInBuildArea,
    private val _height: UShort,
    private val _duration: UShort,
    public val maintainFixedAltitude: Boolean,
    private val _function: UByte,
) : IncomingServerGameMessage {
    public constructor(
        centerXInBuildArea: Int,
        centerZInBuildArea: Int,
        destinationXInBuildArea: Int,
        destinationZInBuildArea: Int,
        height: Int,
        duration: Int,
        maintainFixedAltitude: Boolean,
        function: Int,
    ) : this(
        CoordInBuildArea(centerXInBuildArea, centerZInBuildArea),
        CoordInBuildArea(destinationXInBuildArea, destinationZInBuildArea),
        height.toUShort(),
        duration.toUShort(),
        maintainFixedAltitude,
        function.toUByte(),
    )

    public val centerXInBuildArea: Int
        get() = centerCoordInBuildArea.xInBuildArea
    public val centerZInBuildArea: Int
        get() = centerCoordInBuildArea.zInBuildArea
    public val destinationXInBuildArea: Int
        get() = destinationCoordInBuildArea.xInBuildArea
    public val destinationZInBuildArea: Int
        get() = destinationCoordInBuildArea.zInBuildArea
    public val height: Int
        get() = _height.toInt()
    public val duration: Int
        get() = _duration.toInt()
    public val function: CameraEaseFunction
        get() = CameraEaseFunction[_function.toInt()]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamMoveToEasedCircular

        if (centerCoordInBuildArea != other.centerCoordInBuildArea) return false
        if (destinationCoordInBuildArea != other.destinationCoordInBuildArea) return false
        if (_height != other._height) return false
        if (_duration != other._duration) return false
        if (maintainFixedAltitude != other.maintainFixedAltitude) return false
        if (_function != other._function) return false

        return true
    }

    override fun hashCode(): Int {
        var result = centerCoordInBuildArea.hashCode()
        result = 31 * result + destinationCoordInBuildArea.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + _duration.hashCode()
        result = 31 * result + maintainFixedAltitude.hashCode()
        result = 31 * result + _function.hashCode()
        return result
    }

    override fun toString(): String {
        return "CameraMoveToEasedCircular(" +
            "centerXInBuildArea=$centerXInBuildArea, " +
            "centerZInBuildArea=$centerZInBuildArea, " +
            "destinationXInBuildArea=$destinationXInBuildArea, " +
            "destinationZInBuildArea=$destinationZInBuildArea, " +
            "height=$height, " +
            "duration=$duration, " +
            "maintainFixedAltitude=$maintainFixedAltitude, " +
            "function=$function" +
            ")"
    }
}
