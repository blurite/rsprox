package net.rsprox.protocol.rs3v949.game.outgoing.model.varp

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class VarpSmall private constructor(
    private val _id: UShort,
    private val _value: Byte,
) : IncomingServerGameMessage {
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
        return "VarpSmall(id=$id, value=$value)"
    }
}
