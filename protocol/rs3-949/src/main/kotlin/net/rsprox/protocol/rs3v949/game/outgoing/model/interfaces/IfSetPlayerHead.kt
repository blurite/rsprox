package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetPlayerHead(
    public val componentHash: Long,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetPlayerHead

        return componentHash == other.componentHash
    }

    override fun hashCode(): Int {
        return componentHash.hashCode()
    }

    override fun toString(): String {
        return "IfSetPlayerHead(componentHash=$componentHash)"
    }
}
