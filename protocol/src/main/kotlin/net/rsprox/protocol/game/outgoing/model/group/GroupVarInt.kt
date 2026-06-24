package net.rsprox.protocol.game.outgoing.model.group

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.group.util.GroupVarUpdate

public class GroupVarInt(
    public val update: GroupVarUpdate<Int>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupVarInt

        return update == other.update
    }

    override fun hashCode(): Int {
        return update.hashCode()
    }

    override fun toString(): String {
        return "GroupVarInt(" +
            "update=$update" +
            ")"
    }
}