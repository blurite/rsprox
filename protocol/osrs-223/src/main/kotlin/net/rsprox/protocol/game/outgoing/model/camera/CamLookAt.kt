package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea

/**
 * Cam lookat packet is used to make the camera look towards
 * a certain coordinate in the build area.
 * It is important to note that if this is sent together with
 * a map reload, whether this packet comes before or after the
 * map reload makes a difference - as the build area itself changes.
 *
 * @property destinationXInBuildArea the dest x coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property destinationZInBuildArea the dest z coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property height the height of the camera
 * @property speed the constant speed at which the camera looks towards
 * to the new coordinate
 * @property acceleration the speed increase as the camera looks
 * towards the end coordinate.
 */
public class CamLookAt private constructor(
    private val destinationCoordInBuildArea: CoordInBuildArea,
    private val _height: UShort,
    private val _speed: UByte,
    private val _acceleration: UByte,
) : OutgoingGameMessage {
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

    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamLookAt

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
        return "CamLookAt(" +
            "destinationXInBuildArea=$destinationXInBuildArea, " +
            "destinationZInBuildArea=$destinationZInBuildArea, " +
            "height=$height, " +
            "speed=$speed, " +
            "acceleration=$acceleration" +
            ")"
    }
}
