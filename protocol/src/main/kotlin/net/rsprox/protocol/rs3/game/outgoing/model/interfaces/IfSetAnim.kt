package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetAnim(
    public val componentHash: Long,
    public val animId: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetAnim

        if (componentHash != other.componentHash) return false
        if (animId != other.animId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + animId
        return result
    }

    override fun toString(): String {
        return "IfSetAnim(componentHash=$componentHash, animId=$animId)"
    }
}
