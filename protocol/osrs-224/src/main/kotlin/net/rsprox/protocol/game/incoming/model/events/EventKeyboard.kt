package net.rsprox.protocol.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import java.awt.event.KeyEvent

/**
 * Keyboard events are transmitted at a maximum frequency of every 20 milliseconds.
 * This means that - almost always - a single key is only sent in each packet,
 * as it is very unlikely to get more than one key pressed within a 20-millisecond
 * window, even when trying.
 * While the packet does send the [lastTransmittedKeyPress] per key pressed,
 * there is a flaw in the logic and any subsequent keys after the first will
 * always write a value of 0. For this reason, in order to reduce the memory
 * footprint of this message, we omit any subsequent timestamps and reduce
 * our keys to a byte array value class for even further compression.
 * If the time delta is greater than 16,777,215 milliseconds since the last
 * key transmission, the [lastTransmittedKeyPress] value will be 16,777,215.
 */
public class EventKeyboard(
    public val lastTransmittedKeyPress: Int,
    public val keysPressed: KeySequence,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventKeyboard

        if (lastTransmittedKeyPress != other.lastTransmittedKeyPress) return false
        if (keysPressed != other.keysPressed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lastTransmittedKeyPress
        result = 31 * result + keysPressed.hashCode()
        return result
    }

    override fun toString(): String {
        return "EventKeyboard(" +
            "lastTransmittedKeyPress=$lastTransmittedKeyPress, " +
            "keysPressed=$keysPressed" +
            ")"
    }

    /**
     * KeySequence class represents a sequence of keys pressed in a byte array.
     * This class provides helpful functionality to convert keys from the Jagex
     * format back into the normalized [java.awt.event.KeyEvent] format.
     * @property length the length of the key sequence
     */
    @JvmInline
    public value class KeySequence(
        private val array: ByteArray,
    ) {
        public val length: Int
            get() = array.size

        /**
         * Returns the backing byte array of this key sequence, in Jagex format.
         * It is worth noting that changes done to this array will directly
         * modify this key sequence.
         * All valid keys will be positive byte values.
         */
        public fun asByteArray(): ByteArray {
            return array
        }

        /**
         * Copies this backing key array into an int array, normalizing the
         * values in the process - all keys will be either positive integers,
         * or -1.
         */
        public fun toIntArray(): IntArray {
            return IntArray(length) { index ->
                getJagexKey(index)
            }
        }

        /**
         * Transforms the backing key array into an int array with
         * [java.awt.event.KeyEvent] key codes instead of the compressed
         * Jagex format. Any invalid key will be represented as -1.
         */
        public fun toAwtKeyCodeIntArray(): IntArray {
            return IntArray(length) { index ->
                getAwtKey(index)
            }
        }

        /**
         * Gets the Jagex key code at the provided [index].
         * @param index the index of the key code to obtain.
         * @return Jagex compressed key code, or -1 if the key isn't valid.
         * @throws ArrayIndexOutOfBoundsException if the index is below 0, or >= [length].
         */
        @Throws(ArrayIndexOutOfBoundsException::class)
        public fun getJagexKey(index: Int): Int {
            val code = array[index].toInt() and 0xFF
            return if (code == 0xFF) {
                -1
            } else {
                code
            }
        }

        /**
         * Gets the [java.awt.event.KeyEvent] key code at the provided [index].
         * @param index the index of the key code to obtain.
         * @return [java.awt.event.KeyEvent] key code, or -1 if the key isn't valid.
         * @throws ArrayIndexOutOfBoundsException if the index is below 0, or >= [length].
         */
        @Throws(ArrayIndexOutOfBoundsException::class)
        public fun getAwtKey(index: Int): Int {
            val jagexKey = getJagexKey(index)
            return if (jagexKey == -1) {
                -1
            } else {
                jagexToAwtKeyCodes[jagexKey]
            }
        }

        /**
         * Gets the [java.awt.event.KeyEvent] key code text at the provided [index].
         * @param index the index of the key code text to obtain.
         * @return [java.awt.event.KeyEvent] key code text, or null if the key isn't valid.
         * @throws ArrayIndexOutOfBoundsException if the index is below 0, or >= [length].
         */
        @Throws(ArrayIndexOutOfBoundsException::class)
        public fun getAwtKeyText(index: Int): String? {
            val keyCode = getAwtKey(index)
            return if (keyCode == -1) {
                null
            } else {
                KeyEvent.getKeyText(keyCode)
            }
        }

        private companion object {
            /**
             * The key code translation array found in the client.
             * The trailing -1s have been omitted in this array to shorten the data structure.
             */
            private val awtToJagexKeyCodes =
                intArrayOf(
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    85,
                    80,
                    84,
                    -1,
                    91,
                    -1,
                    -1,
                    -1,
                    81,
                    82,
                    86,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    13,
                    -1,
                    -1,
                    -1,
                    -1,
                    83,
                    104,
                    105,
                    103,
                    102,
                    96,
                    98,
                    97,
                    99,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    25,
                    16,
                    17,
                    18,
                    19,
                    20,
                    21,
                    22,
                    23,
                    24,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    48,
                    68,
                    66,
                    50,
                    34,
                    51,
                    52,
                    53,
                    39,
                    54,
                    55,
                    56,
                    70,
                    69,
                    40,
                    41,
                    32,
                    35,
                    49,
                    36,
                    38,
                    67,
                    33,
                    65,
                    37,
                    64,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    228,
                    231,
                    227,
                    233,
                    224,
                    219,
                    225,
                    230,
                    226,
                    232,
                    89,
                    87,
                    -1,
                    88,
                    229,
                    90,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    11,
                    12,
                    -1,
                    -1,
                    -1,
                    101,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    100,
                    -1,
                    87,
                )

            /**
             * The Jagex key codes to AWT key codes translation array.
             */
            private val jagexToAwtKeyCodes = buildJagexToAwtKeyCodesArray()

            /**
             * Builds a Jagex keycode to AWT key code translation array,
             * used to normalize the keycode events into traditional values.
             */
            private fun buildJagexToAwtKeyCodesArray(): IntArray {
                val keys = IntArray(256) { -1 }
                for ((index, keycode) in awtToJagexKeyCodes.withIndex()) {
                    if (keycode == -1) {
                        continue
                    }
                    keys[keycode] = index
                }
                return keys
            }
        }
    }
}
