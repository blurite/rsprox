package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetObject(
    public val componentHash: Long,
    public val objId: Int,
    public val count: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetObject

        if (componentHash != other.componentHash) return false
        if (objId != other.objId) return false
        if (count != other.count) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + objId
        result = 31 * result + count
        return result
    }

    override fun toString(): String {
        return "IfSetObject(componentHash=$componentHash, objId=$objId, count=$count)"
    }
}
