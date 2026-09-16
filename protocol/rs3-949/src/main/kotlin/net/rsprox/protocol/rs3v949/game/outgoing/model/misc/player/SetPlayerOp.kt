package net.rsprox.protocol.rs3v949.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class SetPlayerOp(
    public val slot: Int,
    public val priority: Boolean,
    public val text: String,
    public val worldId: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetPlayerOp

        if (slot != other.slot) return false
        if (priority != other.priority) return false
        if (text != other.text) return false
        if (worldId != other.worldId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slot
        result = 31 * result + priority.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + worldId
        return result
    }

    override fun toString(): String {
        return "SetPlayerOp(slot=$slot, priority=$priority, text=\"$text\", worldId=$worldId)"
    }
}
