package net.rsprox.protocol.rs3.game.outgoing.model.varp

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class VarpLong private constructor(
    private val _id: UShort,
    public val value: Long,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        value: Long,
    ) : this(
        id.toUShort(),
        value,
    )

    public val id: Int
        get() = _id.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VarpLong

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
        return "VarpLong(id=$id, value=$value)"
    }
}
