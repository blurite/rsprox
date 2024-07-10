package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory
import net.rsprox.protocol.game.outgoing.model.camera.util.CameraEaseFunction
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea

/**
 * Cam look at eased coord is used to make the camera look towards
 * a certain coordinate with various easing functions.
 *
 * @property destinationXInBuildArea the dest x coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property destinationZInBuildArea the dest z coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property height the height of the camera
 * @property duration the duration of the movement in client cycles (20ms/cc)
 * @property function the camera easing function, allowing for finer
 * control over the way it moves from the start coordinate to the end.
 */
public class CamLookAtEasedCoord private constructor(
    private val destinationCoordInBuildArea: CoordInBuildArea,
    private val _height: UShort,
    private val _duration: UShort,
    private val _function: UByte,
) : OutgoingGameMessage {
    public constructor(
        xInBuildArea: Int,
        zInBuildArea: Int,
        height: Int,
        duration: Int,
        function: Int,
    ) : this(
        CoordInBuildArea(xInBuildArea, zInBuildArea),
        height.toUShort(),
        duration.toUShort(),
        function.toUByte(),
    )

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
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamLookAtEasedCoord

        if (destinationCoordInBuildArea != other.destinationCoordInBuildArea) return false
        if (_height != other._height) return false
        if (_duration != other._duration) return false
        if (_function != other._function) return false

        return true
    }

    override fun hashCode(): Int {
        var result = destinationCoordInBuildArea.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + _duration.hashCode()
        result = 31 * result + _function.hashCode()
        return result
    }

    override fun toString(): String {
        return "CamLookAtEasedCoord(" +
            "destinationXInBuildArea=$destinationXInBuildArea, " +
            "destinationZInBuildArea=$destinationZInBuildArea, " +
            "height=$height, " +
            "duration=$duration, " +
            "function=$function" +
            ")"
    }
}
