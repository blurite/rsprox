package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfOpenSub(
    public val componentHash: Long,
    public val childId: Int,
    public val layer: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfOpenSub

        if (componentHash != other.componentHash) return false
        if (childId != other.childId) return false
        if (layer != other.layer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + childId
        result = 31 * result + layer
        return result
    }

    override fun toString(): String {
        return "IfOpenSub(componentHash=$componentHash, childId=$childId, layer=$layer)"
    }
}
