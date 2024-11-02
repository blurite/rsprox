package net.rsprox.protocol.game.outgoing.model.info.playerinfo

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class PlayerInfo(
    public val updates: Map<Int, PlayerUpdateType>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerInfo

        return updates == other.updates
    }

    override fun hashCode(): Int {
        return updates.hashCode()
    }

    override fun toString(): String {
        return "PlayerInfo(updates=$updates)"
    }
}
