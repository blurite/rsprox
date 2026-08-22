package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetNpcHead(
    public val componentHash: Long,
    public val npcId: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetNpcHead

        if (componentHash != other.componentHash) return false
        if (npcId != other.npcId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + npcId
        return result
    }

    override fun toString(): String {
        return "IfSetNpcHead(componentHash=$componentHash, npcId=$npcId)"
    }
}
