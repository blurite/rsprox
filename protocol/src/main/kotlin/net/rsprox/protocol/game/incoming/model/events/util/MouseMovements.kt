package net.rsprox.protocol.game.incoming.model.events.util

/**
 * A class that wraps around an array of mouse movements,
 * with the encoding specified by [MousePosChange].
 * @property length the number of mouse movements in this packet.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class MouseMovements(
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
    public fun asLongArray(): LongArray = movements

    /**
     * Gets the mouse position change at the specified [index]
     * @param index the index at which to obtain the mouse pos change.
     * @return the mouse position change that occurred at that index.
     * @throws ArrayIndexOutOfBoundsException if the index is below 0, or >= [length]
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    public fun getMousePosChange(index: Int): MousePosChange = MousePosChange(movements[index])

    /**
     * A class for mouse position changes, packed into a primitive long.
     * We utilize bitpacking in order to use primitive long arrays for space
     * constraints.
     * @property packed the bitpacked long value, exposed as servers may wish
     * to re-compose the position changes at a later date.
     * @property timeDelta the time difference in client cycles (20ms each) since the last
     * transmitted mouse movement.
     * @property x the x coordinate of the mouse, in pixels. If the
     * mouse goes outside the client window, the value will be -1.
     * @property y the y coordinate of the mouse, in pixels. If the
     * mouse goes outside the client window, the value will be -1.
     * @property lastMouseButton the last mouse button that was clicked shortly
     * before the mouse movement. Value 0 means no recent click, 2 means left
     * mouse click, 8 means right mouse click and 14 means middle mouse click.
     * Other buttons are unknown but may also be possible.
     * The value is 0x7FFF if no mouse button property is included, which is
     * the case for the java variant of this packet.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public class MousePosChange(
        public val packed: Long,
    ) {
        public constructor(
            timeDelta: Int,
            x: Int,
            y: Int,
            delta: Boolean,
        ) : this(
            timeDelta,
            x,
            y,
            delta,
            -1,
        )

        public constructor(
            timeDelta: Int,
            x: Int,
            y: Int,
            delta: Boolean,
            lastMouseButton: Int,
        ) : this(
            pack(timeDelta, x, y, delta, lastMouseButton),
        )

        public val timeDelta: Int
            get() = (packed and 0xFFFF).toInt()
        public val x: Int
            get() = (packed ushr 16 and 0xFFFF).toShort().toInt()
        public val y: Int
            get() = (packed ushr 32 and 0xFFFF).toShort().toInt()
        public val delta: Boolean
            get() = (packed ushr 48 and 0x1).toInt() != 0
        public val lastMouseButton: Int
            get() = (packed ushr 49 and 0x7FFF).toInt()

        override fun toString(): String {
            return if (delta) {
                "MousePosChange(" +
                    "timeDelta=$timeDelta, " +
                    "deltaX=$x, " +
                    "deltaY=$y" +
                    (if (lastMouseButton != 0x7FFF) "lastMouseButton=$lastMouseButton" else "") +
                    ")"
            } else {
                "MousePosChange(" +
                    "timeDelta=$timeDelta, " +
                    "x=$x, " +
                    "y=$y" +
                    (if (lastMouseButton != 0x7FFF) "lastMouseButton=$lastMouseButton" else "") +
                    ")"
            }
        }

        public companion object {
            public fun pack(
                timeDelta: Int,
                x: Int,
                y: Int,
                delta: Boolean,
            ): Long =
                pack(
                    timeDelta,
                    x,
                    y,
                    delta,
                    -1,
                )

            public fun pack(
                timeDelta: Int,
                x: Int,
                y: Int,
                delta: Boolean,
                lastMouseButton: Int,
            ): Long =
                (timeDelta and 0xFFFF)
                    .toLong()
                    .or(x.toLong() and 0xFFFF shl 16)
                    .or(y.toLong() and 0xFFFF shl 32)
                    .or(if (delta) (1L shl 48) else 0)
                    .or(lastMouseButton.toLong() and 0x7FFF shl 49)
        }
    }
}
