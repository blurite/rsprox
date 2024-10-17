package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class WorldEntityInfoV3(
    public val updates: Map<Int, WorldEntityUpdateType>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorldEntityInfoV3

        return updates == other.updates
    }

    override fun hashCode(): Int {
        return updates.hashCode()
    }

    override fun toString(): String {
        return "WorldEntityInfoV3(updates=$updates)"
    }
}
