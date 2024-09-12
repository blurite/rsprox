package net.rsprox.protocol.game.outgoing.model.camera.util

import kotlin.jvm.Throws

/**
 * Camera functions for eased movement.
 * These functions are used together with various 'eased' camera
 * packets to alter how the camera movement happens between the
 * coordinates provided.
 *
 * @property id the respective id of the camera function,
 * as expected by the client.
 */
public enum class CameraEaseFunction(
    public val id: Int,
) {
    LINEAR(0),
    EASE_IN_SINE(1),
    EASE_OUT_SINE(2),
    EASE_IN_OUT_SINE(3),
    EASE_IN_QUAD(4),
    EASE_OUT_QUAD(5),
    EASE_IN_OUT_QUAD(6),
    EASE_IN_CUBIC(7),
    EASE_OUT_CUBIC(8),
    EASE_IN_OUT_CUBIC(9),
    EASE_IN_QUART(10),
    EASE_OUT_QUART(11),
    EASE_IN_OUT_QUART(12),
    EASE_IN_QUINT(13),
    EASE_OUT_QUINT(14),
    EASE_IN_OUT_QUINT(15),
    EASE_IN_EXPO(16),
    EASE_OUT_EXPO(17),
    EASE_IN_OUT_EXPO(18),
    EASE_IN_CIRC(19),
    EASE_OUT_CIRC(20),
    EASE_IN_OUT_CIRC(21),
    EASE_IN_BACK(22),
    EASE_OUT_BACK(23),
    EASE_IN_OUT_BACK(24),
    EASE_IN_ELASTIC(25),
    EASE_OUT_ELASTIC(26),
    EASE_IN_OUT_ELASTIC(27),
    ;

    public fun prettyName(): String {
        return name
            .replace('_', ' ')
            .lowercase()
            .replaceFirstChar { it.uppercaseChar() }
    }

    public companion object {
        /**
         * Gets the camera easing function based on the [id] provided.
         * @throws IndexOutOfBoundsException if the id is below 0 or above 27
         * @return camera ease function
         */
        @Throws(IndexOutOfBoundsException::class)
        public operator fun get(id: Int): CameraEaseFunction {
            // Relying on ordinal here as ordinal aligns with the id values.
            return entries[id]
        }
    }
}
