package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.camera.util.CameraEaseFunction

/**
 * Camera move to arc packet is used to move camera
 * to a new coordinate with finer control behind it.
 * This packet differs from [CamMoveToCyclesV2] in that it will first
 * move through a center coordinate before going towards the destination,
 * creating a `)`-shape movement. An example image of this can be seen
 * [here](https://media.z-kris.com/2024/04/cam%20move%20eased%20circular.png)
 *
 * @property centerX the absolute x coordinate to move through.
 * @property centerZ the absolute z coordinate to move through.
 * @property destinationX the absolute x coordinate to move to.
 * @property destinationZ the absolute z coordinate to move to.
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
public class CamMoveToArcV2 private constructor(
    private val _centerX: UShort,
    private val _centerZ: UShort,
    private val _destinationX: UShort,
    private val _destinationZ: UShort,
    private val _height: UShort,
    private val _cycles: UShort,
    public val ignoreTerrain: Boolean,
    private val _easing: UByte,
) : IncomingServerGameMessage {
    public constructor(
        centerX: Int,
        centerZ: Int,
        destinationX: Int,
        destinationZ: Int,
        height: Int,
        cycles: Int,
        ignoreTerrain: Boolean,
        easing: Int,
    ) : this(
        centerX.toUShort(),
        centerZ.toUShort(),
        destinationX.toUShort(),
        destinationZ.toUShort(),
        height.toUShort(),
        cycles.toUShort(),
        ignoreTerrain,
        easing.toUByte(),
    )

    public val centerX: Int
        get() = _centerX.toInt()
    public val centerZ: Int
        get() = _centerZ.toInt()
    public val destinationX: Int
        get() = _destinationX.toInt()
    public val destinationZ: Int
        get() = _destinationZ.toInt()
    public val height: Int
        get() = _height.toInt()
    public val cycles: Int
        get() = _cycles.toInt()
    public val easing: CameraEaseFunction
        get() = CameraEaseFunction[_easing.toInt()]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamMoveToArcV2

        if (ignoreTerrain != other.ignoreTerrain) return false
        if (_centerX != other._centerX) return false
        if (_centerZ != other._centerZ) return false
        if (_destinationX != other._destinationX) return false
        if (_destinationZ != other._destinationZ) return false
        if (_height != other._height) return false
        if (_cycles != other._cycles) return false
        if (_easing != other._easing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ignoreTerrain.hashCode()
        result = 31 * result + _centerX.hashCode()
        result = 31 * result + _centerZ.hashCode()
        result = 31 * result + _destinationX.hashCode()
        result = 31 * result + _destinationZ.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + _cycles.hashCode()
        result = 31 * result + _easing.hashCode()
        return result
    }

    override fun toString(): String {
        return "CamMoveToArcV2(" +
            "centerX=$centerX, " +
            "centerZ=$centerZ, " +
            "destinationX=$destinationX, " +
            "destinationZ=$destinationZ, " +
            "height=$height, " +
            "cycles=$cycles, " +
            "easing=$easing, " +
            "ignoreTerrain=$ignoreTerrain" +
            ")"
    }
}
