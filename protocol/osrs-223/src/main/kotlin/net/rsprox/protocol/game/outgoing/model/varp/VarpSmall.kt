package net.rsprox.protocol.game.outgoing.model.varp

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Varp small messages are used to send a varp to the client that
 * has a value which fits in the range of a byte, being -128..127.
 * Note that this class does not verify that the value is in the correct
 * range - instead any bits beyond the range of a byte get ignored.
 * @property id the id of the varp
 * @property value the value of the varp, in range of -128 to 127 (inclusive)
 */
public class VarpSmall private constructor(
    private val _id: UShort,
    private val _value: Byte,
) : OutgoingGameMessage {
    public constructor(
        id: Int,
        value: Int,
    ) : this(
        id.toUShort(),
        value.toByte(),
    )

    public val id: Int
        get() = _id.toInt()
    public val value: Int
        get() = _value.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VarpSmall

        if (_id != other._id) return false
        if (_value != other._value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _value
        return result
    }

    override fun toString(): String {
        return "VarpSmall(" +
            "id=$id, " +
            "value=$value" +
            ")"
    }
}
