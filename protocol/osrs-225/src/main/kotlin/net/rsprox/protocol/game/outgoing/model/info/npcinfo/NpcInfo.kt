package net.rsprox.protocol.game.outgoing.model.info.npcinfo

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class NpcInfo(
    public val updates: Map<Int, NpcUpdateType>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NpcInfo

        return updates == other.updates
    }

    override fun hashCode(): Int {
        return updates.hashCode()
    }

    override fun toString(): String {
        return "NpcInfo(updates=$updates)"
    }
}
