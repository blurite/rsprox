package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetModel(
    public val componentHash: Long,
    public val modelId: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetModel

        if (componentHash != other.componentHash) return false
        if (modelId != other.modelId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + modelId
        return result
    }

    override fun toString(): String {
        return "IfSetModel(componentHash=$componentHash, modelId=$modelId)"
    }
}
