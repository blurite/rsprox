package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetHide(
    public val componentHash: Long,
    public val hidden: Boolean,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetHide

        if (componentHash != other.componentHash) return false
        if (hidden != other.hidden) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + hidden.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetHide(componentHash=$componentHash, hidden=$hidden)"
    }
}
