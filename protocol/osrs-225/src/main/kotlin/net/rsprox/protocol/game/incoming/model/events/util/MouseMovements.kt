package net.rsprox.protocol.game.incoming.model.events.util

/**
 * A value class that wraps around an array of mouse movements,
 * with the encoding specified by [MousePosChange].
 * @property length the number of mouse movements in this packet.
 */
@Suppress("MemberVisibilityCanBePrivate")
@JvmInline
public value class MouseMovements(
    private val movements: LongArray,
) {
    public val length: Int
        get() = movements.size

    /**
     * @return the mouse movements data structure as a long array,
     * with encoding as specified by [MousePosChange].
     * It is worth noting the encoding does not match up with the client.
     * However, if people wish to store the mouse movements for later usage,
     * this function provides the backing array which can be reconstructed
     * at a later date.
     * Changes to the backing array will directly reflect on this class.
     */
    public fun asLongArray(): LongArray {
        return movements
    }

    /**
     * Gets the mouse position change at the specified [index]
     * @param index the index at which to obtain the mouse pos change.
     * @return the mouse position change that occurred at that index.
     * @throws ArrayIndexOutOfBoundsException if the index is below 0, or >= [length]
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    public fun getMousePosChange(index: Int): MousePosChange {
        return MousePosChange(movements[index])
    }

    /**
     * A value class for mouse position changes, packed into a primitive long.
     * We utilize bitpacking in order to use primitive long arrays for space
     * constraints.
     * @property packed the bitpacked long value, exposed as servers may wish
     * to re-compose the position changes at a later date.
     * @property timeDelta the time difference in milliseconds since the last
     * transmitted mouse movement.
     * @property xDelta the x coordinate delta of the mouse, in pixels. If the
     * mouse goes outside the client window, the value will be -1.
     * @property yDelta the y coordinate delta of the mouse, in pixels. If the
     * mouse goes outside the client window, the value will be -1.
     * @property lastMouseButton the last mouse button that was clicked shortly
     * before the mouse movement. Value 0 means no recent click, 2 means left
     * mouse click, 8 means right mouse click and 14 means middle mouse click.
     * Other buttons are unknown but may also be possible.
     * The value is 0xFFFF if no mouse button property is included, which is
     * the case for the java variant of this packet.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    @JvmInline
    public value class MousePosChange(
        public val packed: Long,
    ) {
        public constructor(
            timeDelta: Int,
            xDelta: Int,
            yDelta: Int,
        ) : this(
            timeDelta,
            xDelta,
            yDelta,
            -1,
        )

        public constructor(
            timeDelta: Int,
            xDelta: Int,
            yDelta: Int,
            lastMouseButton: Int,
        ) : this(
            (timeDelta and 0xFFFF)
                .toLong()
                .or(xDelta.toLong() and 0xFFFF shl 16)
                .or(yDelta.toLong() and 0xFFFF shl 32)
                .or(lastMouseButton.toLong() and 0xFFFF shl 48),
        )

        public val timeDelta: Int
            get() = (packed and 0xFFFF).toInt()
        public val xDelta: Int
            get() = (packed ushr 16 and 0xFFFF).toShort().toInt()
        public val yDelta: Int
            get() = (packed ushr 32 and 0xFFFF).toShort().toInt()
        public val lastMouseButton: Int
            get() = (packed ushr 48 and 0xFFFF).toInt()

        override fun toString(): String {
            return "MousePosChange(" +
                "timeDelta=$timeDelta, " +
                "xDelta=$xDelta, " +
                "yDelta=$yDelta" +
                (if (lastMouseButton != 0xFFFF) "lastMouseButton=$lastMouseButton" else "") +
                ")"
        }
    }
}
