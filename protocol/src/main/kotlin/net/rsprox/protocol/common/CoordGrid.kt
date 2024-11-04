package net.rsprox.protocol.common

/**
 * Coord grid, commonly referred to just as Coordinate or Location,
 * is responsible for tracking absolute positions of avatars in the game.
 * @param packed the 30-bit bitpacked integer representing the coord grid.
 */
@JvmInline
public value class CoordGrid(
    public val packed: Int,
) {
    /**
     * @param level the height level of the avatar.
     * @param x the absolute x coordinate of the avatar.
     * @param z the absolute z coordinate of the avatar.
     */
    @Suppress("ConvertTwoComparisonsToRangeCheck")
    public constructor(
        level: Int,
        x: Int,
        z: Int,
    ) : this(
        (level shl 28)
            .or(x shl 14)
            .or(z),
    ) {
        // https://youtrack.jetbrains.com/issue/KT-62798/in-range-checks-are-not-intrinsified-in-kotlin-stdlib
        // Using traditional checks to avoid generating range objects (seen by decompiling this class)
        require(level >= 0 && level < 4) {
            "Level must be in range of 0..<4: $level"
        }
        require(x >= 0 && x <= 16384) {
            "X coordinate must be in range of 0..<16384: $x"
        }
        require(z >= 0 && z <= 16384) {
            "Z coordinate must be in range of 0..<16384, $z"
        }
    }

    public val level: Int
        get() = packed ushr 28
    public val x: Int
        get() = packed ushr 14 and 0x3FFF
    public val z: Int
        get() = packed and 0x3FFF

    /**
     * Checks whether this coord grid is within [distance] of the [other] coord grid.
     * If the coord grids are on different levels, this function will always return false.
     * @param other the other coord grid to check against.
     * @param distance the distance to check (inclusive). A distance of 0 implies same coordinate.
     * @return true if the [other] coord grid is within [distance] of this coord grid.
     */
    public fun inDistance(
        other: CoordGrid,
        distance: Int,
    ): Boolean {
        if (level != other.level) {
            return false
        }
        val deltaX = x - other.x
        if (deltaX !in -distance..distance) {
            return false
        }
        val deltaZ = z - other.z
        return deltaZ in -distance..distance
    }

    /**
     * Checks if this coord grid is uninitialized.
     * Uninitialized coord grids are determined by checking if all 32 bits of
     * the [packed] property are enabled (including sign bit, which would be the opposite).
     * As the main constructor of this class only takes in the components that build a coord grid,
     * it is impossible to make an instance of this that matches the invalid value,
     * unless directly using the single-argument constructor.
     */
    @Suppress("NOTHING_TO_INLINE")
    public inline fun invalid(): Boolean {
        return this == INVALID
    }

    public operator fun component1(): Int {
        return level
    }

    public operator fun component2(): Int {
        return x
    }

    public operator fun component3(): Int {
        return z
    }

    override fun toString(): String {
        return "CoordGrid(" +
            "level=$level, " +
            "x=$x, " +
            "z=$z" +
            ")"
    }

    public companion object {
        public val INVALID: CoordGrid = CoordGrid(-1)
    }
}
