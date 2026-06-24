package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Cam move to packet is used to move the position of the camera
 * to a specific coordinate in the root world.
 *
 * @property x the absolute x coordinate to move to.
 * @property z the absolute z coordinate to move to.
 * @property height the height of the camera
 * @property rate the constant speed at which the camera moves
 * to the new coordinate
 * @property rate2 the speed increase as the camera moves
 * towards the end coordinate.
 * @property heightRelative whether the height is relative to the
 * previous camera move-to packet's height.
 */
public class CamMoveToV3 private constructor(
    private val _x: UShort,
    private val _z: UShort,
    private val _height: Short,
    private val _rate: UByte,
    private val _rate2: UByte,
    public val heightRelative: Boolean,
) : IncomingServerGameMessage {
    public constructor(
        x: Int,
        z: Int,
        height: Int,
        rate: Int,
        rate2: Int,
        heightRelative: Boolean,
    ) : this(
        x.toUShort(),
        z.toUShort(),
        height.toShort(),
        rate.toUByte(),
        rate2.toUByte(),
        heightRelative,
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

        other as CamMoveToV3

        if (_x != other._x) return false
        if (_z != other._z) return false
        if (_height != other._height) return false
        if (_rate != other._rate) return false
        if (_rate2 != other._rate2) return false
        if (heightRelative != other.heightRelative) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _x.hashCode()
        result = 31 * result + _z.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + _rate.hashCode()
        result = 31 * result + _rate2.hashCode()
        result = 31 * result + heightRelative.hashCode()
        return result
    }

    override fun toString(): String {
        return "CamMoveToV3(" +
            "x=$x, " +
            "z=$z, " +
            "height=$height, " +
            "rate=$rate, " +
            "rate2=$rate2, " +
            "heightRelative=$heightRelative" +
            ")"
    }
}
