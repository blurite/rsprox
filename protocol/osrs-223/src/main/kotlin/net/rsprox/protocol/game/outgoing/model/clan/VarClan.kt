package net.rsprox.protocol.game.outgoing.model.clan

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Var clans are used to transmit a variable of a clan to the user.
 * It is important to note that the data type must align with what
 * is defined in the cache, or the client will not be decoding it
 * correctly, which will most likely lead to a disconnection.
 * @property id the id of the varclan
 * @property value the varclan data value.
 * Use [VarClanIntData], [VarClanLongData] or [VarClanStringData] to
 * transmit the payload, depending on the defined type in the cache.
 */
public class VarClan private constructor(
    private val _id: UShort,
    public val value: VarClanData,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        value: VarClanData,
    ) : this(
        id.toUShort(),
        value,
    )

    public val id: Int
        get() = _id.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VarClan

        if (_id != other._id) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "VarClan(" +
            "id=$id, " +
            "value=$value" +
            ")"
    }

    public sealed interface VarClanData

    /**
     * Unknown var clan data is instantiated when we are decoding the
     * packet from the client's perspective. The client relies on the cache
     * in order to determine the type of the varclan to decode.
     * As we do not have access to the cache at this time, we simply store
     * the raw bytes that make up the value. As such, the decoding logic
     * is passed along to the transcriber, which could be made to use the
     * cache in order to figure out the type at hand.
     *
     * It is technically possible to try and infer the type, but it is not
     * reliable, as strings could be of any structure and overlap with
     * what is identified as integers or longs.
     *
     * @property data the payload of the packet, to be decoded by the transcriber
     * when sufficient information is available.
     */
    public class UnknownVarClanData(
        public val data: ByteArray,
    ) : VarClanData {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UnknownVarClanData

            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        override fun toString(): String {
            return "UnknownVarClanData(data=${data.contentToString()})"
        }
    }

    /**
     * Var clan int data is used to transmit a 32-bit integer as a varclan
     * value.
     * @property value the 32-bit integer value for this varclan.
     */
    public class VarClanIntData(
        public val value: Int,
    ) : VarClanData {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as VarClanIntData

            return value == other.value
        }

        override fun hashCode(): Int {
            return value
        }

        override fun toString(): String {
            return "VarClanIntData(value=$value)"
        }
    }

    /**
     * Var clan int data is used to transmit a 64-bit long as a varclan
     * value.
     * @property value the 64-bit long value for this varclan.
     */
    public class VarClanLongData(
        public val value: Long,
    ) : VarClanData {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as VarClanLongData

            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "VarClanLongData(value=$value)"
        }
    }

    /**
     * Var clan int data is used to transmit a string as a varclan
     * value.
     * @property value the string for this varclan.
     */
    public class VarClanStringData(
        public val value: String,
    ) : VarClanData {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as VarClanStringData

            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "VarClanStringData(value='$value')"
        }
    }
}
