package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class WorldEntityInfo(
    public val updates: Map<Int, WorldEntityUpdateType>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorldEntityInfo

        return updates == other.updates
    }

    override fun hashCode(): Int {
        return updates.hashCode()
    }

    override fun toString(): String {
        return "WorldEntityInfo(updates=$updates)"
    }
}
