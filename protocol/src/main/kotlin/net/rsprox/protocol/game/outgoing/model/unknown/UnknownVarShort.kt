package net.rsprox.protocol.game.outgoing.model.unknown

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UnknownVarShort(
    public val value: Int,
    public val remainingBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownVarShort

        if (value != other.value) return false
        if (!remainingBytes.contentEquals(other.remainingBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + remainingBytes.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "UnknownVarShort(" +
            "value=$value, " +
            "remainingBytes=${remainingBytes.contentToString()}" +
            ")"
    }
}
