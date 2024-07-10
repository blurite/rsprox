package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory
import net.rsprox.protocol.game.outgoing.model.camera.util.CameraEaseFunction

/**
 * Cam look at eased angle relative is used to make the camera look towards
 * an angle relative to the current camera angle.
 * One way to think of this packet is that it **adds** values to the
 * x and y angles of the camera.
 *
 * @property xAngle the additional angle to add to the x-axis of the camera.
 * It's worth noting that the x angle of the camera ranges between 128 and
 * 383 (inclusive), and the resulting value is coerced in that range.
 * Negative values are also accepted.
 * Additionally, there is currently a bug in the client that causes the
 * third and the fifth least significant bits of the resulting angle to
 * be discarded due to the code doing (cameraXAngle + [xAngle] & 2027),
 * which is further coerced into the 128-383 range.
 * @property yAngle the additional angle to add to the y-axis of the camera.
 * Unlike the x-axis angle, this one ranges from 0 to 2047 (inclusive),
 * and does not get coerced - instead it will just roll over (e.g. 2047 -> 0).
 * @property duration the duration of the movement in client cycles (20ms/cc)
 * @property function the camera easing function, allowing for finer
 * control over the way it moves from the start coordinate to the end.
 */
public class CamLookAtEasedAngleRelative private constructor(
    private val _xAngle: Short,
    private val _yAngle: Short,
    private val _duration: UShort,
    private val _function: UByte,
) : OutgoingGameMessage {
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
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamLookAtEasedAngleRelative

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
        return "CamLookAtEasedAngleRelative(" +
            "xAngle=$xAngle, " +
            "yAngle=$yAngle, " +
            "duration=$duration, " +
            "function=$function" +
            ")"
    }
}
