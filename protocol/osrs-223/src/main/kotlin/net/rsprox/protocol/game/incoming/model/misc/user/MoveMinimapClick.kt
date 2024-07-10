package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprox.protocol.game.incoming.model.misc.user.internal.MovementRequest
import kotlin.math.cos
import kotlin.math.sin

/**
 * Move minimap click is sent when the player requests to walk somewhere
 * through their minimap.
 * While the packet itself sends additional constant values to the server,
 * we do not store those values as they are expected to always be the same.
 * The decoder will verify the values and throw an exception in decoding
 * if those values do not align up.
 * @property x the absolute x coordinate the player is walking to
 * @property z the absolute z coordinate the player is walking to
 * @property keyCombination the combination of keys held down to move there.
 * Possible values include 0, 1 and 2, where:
 * A value of 2 is sent if the user is holding down the 'Control' and 'Shift' keys
 * simultaneously.
 * A value of 1 is sent if the user is holding down the 'Control' key without
 * the 'Shift' key.
 * In any other scenario, a value of 0 is sent.
 * The 'Control' key is used to invert move speed for the single movement request,
 * and the 'Control' + 'Shift' combination is presumably for J-Mods to teleport
 * around - although there are no validations for J-Mod privileges in the client,
 * it will send the value of 2 even for regular users.
 * @property minimapWidth the width of the minimap component in pixels
 * @property minimapHeight the height of the minimap component in pixels
 * @property cameraAngleY the angle of the camera
 * @property fineX the fine x coordinate of the local player
 * @property fineZ the fine z coordinate of the local player
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class MoveMinimapClick private constructor(
    private val movementRequest: MovementRequest,
    private val _minimapWidth: UByte,
    private val _minimapHeight: UByte,
    private val _cameraAngleY: UShort,
    private val _fineX: UShort,
    private val _fineZ: UShort,
) : IncomingGameMessage {
    public constructor(
        x: Int,
        z: Int,
        keyCombination: Int,
        minimapWidth: Int,
        minimapHeight: Int,
        cameraAngleY: Int,
        fineX: Int,
        fineZ: Int,
    ) : this(
        MovementRequest(
            x,
            z,
            keyCombination,
        ),
        minimapWidth.toUByte(),
        minimapHeight.toUByte(),
        cameraAngleY.toUShort(),
        fineX.toUShort(),
        fineZ.toUShort(),
    )

    public val x: Int
        get() = movementRequest.x
    public val z: Int
        get() = movementRequest.z
    public val keyCombination: Int
        get() = movementRequest.keyCombination
    public val minimapWidth: Int
        get() = _minimapWidth.toInt()
    public val minimapHeight: Int
        get() = _minimapHeight.toInt()
    public val cameraAngleY: Int
        get() = _cameraAngleY.toInt()
    public val fineX: Int
        get() = _fineX.toInt()
    public val fineZ: Int
        get() = _fineZ.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    /**
     * Checks if the provided arguments are valid (as in produce the same
     * end coordinates if re-calculated).
     * It is worth noting that the C++ client has zoom functionality for
     * which the server does not get information, so it is not possible
     * to verify this on the C++ clients. Additionally, clients such as
     * RuneLite have their own built-in zoom and also do not correct the
     * packet itself. For these reasons, the verification is not done by
     * the library, but it could provide a useful bit of information for
     * complete vanilla builds.
     * @param baseZoneX the south-western zone x coordinate of the build area
     * @param baseZoneZ the south-western zone z coordinate of the build area
     * The base zone coordinates are relative to the build-area that the client
     * builds around. If the player logs in at absolute coordinates 3200, 3220,
     * their baseZoneX would be ((3200 - (6 * 8)) / 8), and the baseZoneZ
     * would be ((3220 - (6 * 8)) / 8), resulting in base zone coordinates of
     * 394, 396. The (6 * 8) is the normal subtraction to go from the center
     * of the build-area to the south-western corner, as a value of 48 is
     * subtracted in the case of a size 104 build-area.
     */
    public fun isValid(
        baseZoneX: Int,
        baseZoneZ: Int,
    ): Boolean {
        val minimapAngle = cameraAngleY and 0x7FF
        val sine = sine[minimapAngle]
        val cosine = cosine[minimapAngle]
        val minimapX = ((cosine * minimapWidth) + (sine * minimapHeight)) shr 11
        val minimapY = ((cosine * minimapHeight) - (sine * minimapWidth)) shr 11
        val localX = (minimapX + fineX) shr 7
        val localY = (fineZ - minimapY) shr 7
        val calculatedDestX = (baseZoneX shl 3) + localX
        val calculatedDestZ = (baseZoneZ shl 3) + localY
        return calculatedDestX == x && calculatedDestZ == z
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MoveMinimapClick

        if (movementRequest != other.movementRequest) return false
        if (_minimapWidth != other._minimapWidth) return false
        if (_minimapHeight != other._minimapHeight) return false
        if (_cameraAngleY != other._cameraAngleY) return false
        if (_fineX != other._fineX) return false
        if (_fineZ != other._fineZ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = movementRequest.hashCode()
        result = 31 * result + _minimapWidth.hashCode()
        result = 31 * result + _minimapHeight.hashCode()
        result = 31 * result + _cameraAngleY.hashCode()
        result = 31 * result + _fineX.hashCode()
        result = 31 * result + _fineZ.hashCode()
        return result
    }

    override fun toString(): String {
        return "MoveMinimapClick(" +
            "x=$x, " +
            "z=$z, " +
            "keyCombination=$keyCombination, " +
            "width=$minimapWidth, " +
            "height=$minimapHeight, " +
            "cameraAngleY=$cameraAngleY, " +
            "fineX=$fineX, " +
            "fineZ=$fineZ" +
            ")"
    }

    private companion object {
        private const val MAX_ANGLE = 65536.0
        private const val CONSTANT = 0.0030679615
        private val sine: IntArray =
            IntArray(2048) {
                (MAX_ANGLE * sin(it * CONSTANT)).toInt()
            }
        private val cosine: IntArray =
            IntArray(2048) {
                (MAX_ANGLE * cos(it * CONSTANT)).toInt()
            }
    }
}
