package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetScrollPos(
    public val componentHash: Long,
    public val scrollPos: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetScrollPos

        if (componentHash != other.componentHash) return false
        if (scrollPos != other.scrollPos) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + scrollPos
        return result
    }

    override fun toString(): String {
        return "IfSetScrollPos(componentHash=$componentHash, scrollPos=$scrollPos)"
    }
}
