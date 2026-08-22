package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetEvents(
    public val componentHash: Long,
    public val fromSlot: Int,
    public val toSlot: Int,
    public val settings: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetEvents

        if (componentHash != other.componentHash) return false
        if (fromSlot != other.fromSlot) return false
        if (toSlot != other.toSlot) return false
        if (settings != other.settings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + fromSlot
        result = 31 * result + toSlot
        result = 31 * result + settings
        return result
    }

    override fun toString(): String {
        return "IfSetEvents(componentHash=$componentHash, fromSlot=$fromSlot, toSlot=$toSlot, settings=$settings)"
    }
}
