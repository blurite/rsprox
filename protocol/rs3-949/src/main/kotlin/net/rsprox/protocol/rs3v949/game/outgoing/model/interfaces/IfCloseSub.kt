package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfCloseSub(
    public val parentComponentHash: Long,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfCloseSub

        return parentComponentHash == other.parentComponentHash
    }

    override fun hashCode(): Int {
        return parentComponentHash.hashCode()
    }

    override fun toString(): String {
        return "IfCloseSub(parentComponentHash=$parentComponentHash)"
    }
}
