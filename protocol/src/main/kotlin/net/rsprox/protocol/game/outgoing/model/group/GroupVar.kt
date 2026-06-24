package net.rsprox.protocol.game.outgoing.model.group

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.group.util.GroupVarUpdate

public class GroupVar(
    public val updates: List<GroupVarUpdate<*>>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupVar

        return updates == other.updates
    }

    override fun hashCode(): Int {
        return updates.hashCode()
    }

    override fun toString(): String {
        return "GroupVar(" +
            "updates=$updates" +
            ")"
    }
}
