package net.rsprox.protocol.game.outgoing.model.unknown

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UnknownString(
    public val string: String,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownString

        return string == other.string
    }

    override fun hashCode(): Int {
        return string.hashCode()
    }

    override fun toString(): String {
        return "UnknownString(string='$string')"
    }
}
