package net.rsprox.protocol.common

/**
 * CoordFine represents a precise coordinate in the client. The values here are [CoordGrid]'s
 * values, multiplier by 128 for x/z coordinates.
 */
@JvmInline
public value class CoordFine(
    public val packed: Long,
) {
    /**
     * @param x the absolute fine x coordinate of the avatar.
     * @param y the fine y coordinate (or height) of this avatar.
     * @param z the absolute fine z coordinate of the avatar.
     */
    @Suppress("ConvertTwoComparisonsToRangeCheck")
    public constructor(
        x: Int,
        y: Int,
        z: Int,
    ) : this(
        (y.toLong() shl 42)
            .or(x.toLong() shl 21)
            .or(z.toLong()),
    ) {
        require(y in 0..<1_024) {
            "Y coordinate must be in range of 0..<1_024: $y"
        }
        require(x in 0..2_097_151) {
            "X coordinate must be in range of 0..<2_097_151: $x"
        }
        require(z in 0..2_097_151) {
            "Z coordinate must be in range of 0..<2_097_151, $z"
        }
    }

    public val x: Int
        get() = (packed ushr 21 and 0x1FFFFF).toInt()
    public val y: Int
        get() = (packed ushr 42).toInt()
    public val z: Int
        get() = (packed and 0x1FFFFF).toInt()

    /**
     * Converts the coord fine into a coord grid.
     * @param level the level which to return. This is because the [y] coordinate is not
     * linked to the actual level at which the coord exists.
     * @return CoordGrid that aligns with this CoordFine.
     */
    public fun toCoordGrid(level: Int): CoordGrid {
        return CoordGrid(level, x ushr 7, z ushr 7)
    }

    override fun toString(): String {
        return "CoordFine(" +
            "x=$x, " +
            "y=$y, " +
            "z=$z" +
            ")"
    }

    public companion object {
        public val INVALID: CoordFine = CoordFine(-1)
    }
}
