package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.camera.util.CameraEaseFunction

/**
 * Camera move to cycles packet is used to move camera
 * to a new coordinate with finer control behind it.
 * @property x the absolute x coordinate to move to.
 * @property z the absolute z coordinate to move to.
 * @property height the height of the camera once it arrives at the destination
 * @property cycles the duration of the movement in client cycles (20ms/cc)
 * @property ignoreTerrain whether the camera moves along the terrain,
 * moving up and down according to bumps in the terrain.
 * If true, the camera will move in a straight line from the starting position
 * towards the end position, ignoring any changes in the terrain.
 * @property easing the camera easing function, allowing for finer
 * control over the way it moves from the start coordinate to the end.
 */
@Suppress("DuplicatedCode")
public class CamMoveToCyclesV2 private constructor(
    private val _x: UShort,
    private val _z: UShort,
    private val _height: UShort,
    private val _cycles: UShort,
    public val ignoreTerrain: Boolean,
    private val _easing: UByte,
) : IncomingServerGameMessage {
    public constructor(
        x: Int,
        z: Int,
        height: Int,
        cycles: Int,
        ignoreTerrain: Boolean,
        easing: Int,
    ) : this(
        x.toUShort(),
        z.toUShort(),
        height.toUShort(),
        cycles.toUShort(),
        ignoreTerrain,
        easing.toUByte(),
    )

    public val x: Int
        get() = _x.toInt()
    public val z: Int
        get() = _z.toInt()
    public val height: Int
        get() = _height.toInt()
    public val cycles: Int
        get() = _cycles.toInt()
    public val easing: CameraEaseFunction
        get() = CameraEaseFunction[_easing.toInt()]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamMoveToCyclesV2

        if (ignoreTerrain != other.ignoreTerrain) return false
        if (_x != other._x) return false
        if (_z != other._z) return false
        if (_height != other._height) return false
        if (_cycles != other._cycles) return false
        if (_easing != other._easing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ignoreTerrain.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _z.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + _cycles.hashCode()
        result = 31 * result + _easing.hashCode()
        return result
    }

    override fun toString(): String {
        return "CamMoveToCyclesV2(" +
            "x=$x, " +
            "z=$z, " +
            "height=$height, " +
            "cycles=$cycles, " +
            "easing=$easing, " +
            "ignoreTerrain=$ignoreTerrain" +
            ")"
    }
}
