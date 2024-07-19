package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.camera.util.CameraEaseFunction

/**
 * Cam rotateto is used to make the camera look towards
 * an angle relative to the current camera angle.
 * One way to think of this packet is that it **adds** values to the
 * x and y angles of the camera.
 *
 * @property xAngle the x angle of the camera to set to.
 * Note that the angle is coerced into a range of 128..383,
 * and incorrectly excludes the third and fifth least significant bits
 * before doing so (by doing [xAngle] & 2027, rather than 2047).
 * @property yAngle the x angle of the camera to set to.
 * Note that the angle incorrectly excludes the third and fifth least significant bits
 * (by doing [xAngle] & 2027, rather than 2047).
 * @property duration the duration of the movement in client cycles (20ms/cc)
 * @property function the camera easing function, allowing for finer
 * control over the way it moves from the start coordinate to the end.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class CamRotateTo private constructor(
    private val _xAngle: Short,
    private val _yAngle: Short,
    private val _duration: UShort,
    private val _function: UByte,
) : IncomingServerGameMessage {
    public constructor(
        xAngle: Int,
        yAngle: Int,
        duration: Int,
        function: Int,
    ) : this(
        xAngle.toShort(),
        yAngle.toShort(),
        duration.toUShort(),
        function.toUByte(),
    )

    public val xAngle: Int
        get() = _xAngle.toInt()
    public val yAngle: Int
        get() = _yAngle.toInt()
    public val duration: Int
        get() = _duration.toInt()
    public val function: CameraEaseFunction
        get() = CameraEaseFunction[_function.toInt()]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamRotateTo

        if (_xAngle != other._xAngle) return false
        if (_yAngle != other._yAngle) return false
        if (_duration != other._duration) return false
        if (_function != other._function) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _xAngle.toInt()
        result = 31 * result + _yAngle
        result = 31 * result + _duration.hashCode()
        result = 31 * result + _function.hashCode()
        return result
    }

    override fun toString(): String {
        return "CamRotateTo(" +
            "xAngle=$xAngle, " +
            "yAngle=$yAngle, " +
            "duration=$duration, " +
            "function=$function" +
            ")"
    }
}
