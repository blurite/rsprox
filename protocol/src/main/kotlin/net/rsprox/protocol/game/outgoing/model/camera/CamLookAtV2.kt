package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Cam lookat packet is used to make the camera look towards
 * a certain coordinate in the root world.
 *
 * @property x the absolute x coordinate to look at.
 * @property z the absolute z coordinate to look at.
 * @property height the height of the camera
 * @property rate the constant speed at which the camera looks towards
 * to the new coordinate
 * @property rate2 the speed increase as the camera looks
 * towards the end coordinate.
 */
public class CamLookAtV2 private constructor(
    private val _x: UShort,
    private val _z: UShort,
    private val _height: UShort,
    private val _rate: UByte,
    private val _rate2: UByte,
) : IncomingServerGameMessage {
    public constructor(
        x: Int,
        z: Int,
        height: Int,
        rate: Int,
        rate2: Int,
    ) : this(
        x.toUShort(),
        z.toUShort(),
        height.toUShort(),
        rate.toUByte(),
        rate2.toUByte(),
    )

    public val x: Int
        get() = _x.toInt()
    public val z: Int
        get() = _z.toInt()
    public val height: Int
        get() = _height.toInt()
    public val rate: Int
        get() = _rate.toInt()
    public val rate2: Int
        get() = _rate2.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamLookAtV2

        if (_x != other._x) return false
        if (_z != other._z) return false
        if (_height != other._height) return false
        if (_rate != other._rate) return false
        if (_rate2 != other._rate2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _x.hashCode()
        result = 31 * result + _z.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + _rate.hashCode()
        result = 31 * result + _rate2.hashCode()
        return result
    }

    override fun toString(): String {
        return "CamLookAtV2(" +
            "x=$x, " +
            "z=$z, " +
            "height=$height, " +
            "rate=$rate, " +
            "rate2=$rate2" +
            ")"
    }
}
