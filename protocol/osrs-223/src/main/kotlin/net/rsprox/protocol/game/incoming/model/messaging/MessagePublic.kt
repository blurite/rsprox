package net.rsprox.protocol.game.incoming.model.messaging

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * Message public events are sent when the player talks in public.
 *
 * Chat types table:
 * ```
 * | Id |        Type        |
 * |----|:------------------:|
 * | 0  |       Normal       |
 * | 1  |      Autotyper     |
 * | 2  |   Friend channel   |
 * | 3  |  Clan main channel |
 * | 4  | Clan guest channel |
 * ```
 *
 * Colour table:
 * ```
 * | Id    | Prefix    |          Hex Value         |
 * |-------|-----------|:--------------------------:|
 * | 0     | yellow:   |          0xFFFF00          |
 * | 1     | red:      |          0xFF0000          |
 * | 2     | green:    |          0x00FF00          |
 * | 3     | cyan:     |          0x00FFFF          |
 * | 4     | purple:   |          0xFF00FF          |
 * | 5     | white:    |          0xFFFFFF          |
 * | 6     | flash1:   |      0xFF0000/0xFFFF00     |
 * | 7     | flash2:   |      0x0000FF/0x00FFFF     |
 * | 8     | flash3:   |      0x00B000/0x80FF80     |
 * | 9     | glow1:    | 0xFF0000-0xFFFF00-0x00FFFF |
 * | 10    | glow2:    | 0xFF0000-0x00FF00-0x0000FF |
 * | 11    | glow3:    | 0xFFFFFF-0x00FF00-0x00FFFF |
 * | 12    | rainbow:  |             N/A            |
 * | 13-20 | pattern*: |             N/A            |
 * ```
 *
 * Effects table:
 * ```
 * | Id | Prefix  |
 * |----|---------|
 * | 1  | wave:   |
 * | 2  | wave2:  |
 * | 3  | shake:  |
 * | 4  | scroll: |
 * | 5  | slide:  |
 * ```
 *
 * Clan types table:
 * ```
 * | Id |      Type     |
 * |----|:-------------:|
 * | 0  |  Normal clan  |
 * | 1  | Group ironman |
 * | 2  |   PvP Arena   |
 * ```
 *
 * @property type the type of the message, ranging from 0 to 4 (inclusive) (see above)
 * @property colour the colour of the message, ranging from 0 to 20 (inclusive) (see above)
 * @property effect the effect of the message, ranging from 0 to 5 (inclusive) (see above)
 * @property message the message typed
 * @property pattern the colour pattern attached to the message, if the [colour] value is
 * in range of 13-20 (inclusive), otherwise null
 * @property clanType the clan type, if the [type] is the main clan channel,
 * a value of 0 to 2 (inclusive) is provided. If the clan type is not defined,
 * the value of -1 is given.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class MessagePublic private constructor(
    private val _type: UByte,
    private val _colour: UByte,
    private val _effect: UByte,
    public val message: String,
    public val pattern: MessageColourPattern?,
    private val _clanType: Byte,
) : IncomingGameMessage {
    public constructor(
        type: Int,
        colour: Int,
        effect: Int,
        message: String,
        pattern: MessageColourPattern?,
        clanType: Int,
    ) : this(
        type.toUByte(),
        colour.toUByte(),
        effect.toUByte(),
        message,
        pattern,
        clanType.toByte(),
    )

    public val type: Int
        get() = _type.toInt()
    public val colour: Int
        get() = _colour.toInt()
    public val effect: Int
        get() = _effect.toInt()
    public val clanType: Int
        get() = _clanType.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessagePublic

        if (_type != other._type) return false
        if (_colour != other._colour) return false
        if (_effect != other._effect) return false
        if (message != other.message) return false
        if (pattern != other.pattern) return false
        if (_clanType != other._clanType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _type.hashCode()
        result = 31 * result + _colour.hashCode()
        result = 31 * result + _effect.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + (pattern?.hashCode() ?: 0)
        result = 31 * result + _clanType
        return result
    }

    override fun toString(): String {
        return "MessagePublicEvent(" +
            "message='$message', " +
            "pattern=$pattern, " +
            "type=$type, " +
            "colour=$colour, " +
            "effect=$effect, " +
            "clanType=$clanType" +
            ")"
    }

    /**
     * A value class for message colour patterns, allowing easy
     * conversion from the byte array to the respective 24-bit RGB colours.
     * This wrapper class additionally provides a helpful [isValid] function,
     * as it is possible to otherwise send bad data from the client and
     * crash the players in vicinity.
     */
    @JvmInline
    public value class MessageColourPattern(
        private val bytes: ByteArray,
    ) {
        public val length: Int
            get() = bytes.size

        /**
         * @return the backing byte array for the pattern.
         * Changes done to this array will reflect on the pattern itself.
         */
        public fun asByteArray(): ByteArray {
            return bytes
        }

        /**
         * @return a copy of the backing pattern byte array.
         */
        public fun toByteArray(): ByteArray {
            return bytes.copyOf()
        }

        /**
         * Checks if the pattern itself is valid (as in, will not crash the client).
         * The client's own checks are currently slightly flawed and allow for
         * crashes to occur in one particular manner.
         * @return whether the pattern is valid
         */
        public fun isValid(): Boolean {
            if (length !in 1..8) {
                return false
            }
            for (i in bytes.indices) {
                val value = bytes[i].toInt()
                if (value < 0 || value >= colourCodes.size) {
                    return false
                }
            }
            return true
        }

        /**
         * Turns the pattern into a 24-bit RGB colour array, if it is valid.
         * @return 24-bit RGB colour array of this pattern, or null if the pattern
         * is corrupt.
         */
        public fun to24BitRgbOrNull(): IntArray? {
            if (length !in 1..8) {
                return null
            }
            val colours = IntArray(length)
            for (i in bytes.indices) {
                val colourCode =
                    colourCodes.getOrNull(bytes[i].toInt())
                        ?: return null
                colours[i] = colourCode
            }
            return colours
        }

        override fun toString(): String {
            return "MessageColourPattern(bytes=${bytes.contentToString()})"
        }

        private companion object {
            private val colourCodes =
                intArrayOf(
                    0xffffff,
                    0xe40303,
                    0xff8c00,
                    0xffed00,
                    0x8026,
                    0x24408e,
                    0x732982,
                    0xff218c,
                    0xb55690,
                    0x5049cc,
                    0xa3a3a3,
                    0xd52d00,
                    0xef7627,
                    0xfcf434,
                    0x78d70,
                    0x21b1ff,
                    0x9b4f96,
                    0xffafc7,
                    0xd162a4,
                    0x7bade3,
                    0xff9a56,
                    0x26ceaa,
                    0x73d7ee,
                    0x9c59d1,
                    0x98e8c1,
                    0xb57edc,
                    0x2c2c2c,
                    0x940202,
                    0x613915,
                    0xd0c100,
                    0x4a8123,
                    0x38a8,
                    0x800080,
                    0xd60270,
                    0xa30262,
                    0x3d1a78,
                )
        }
    }
}
