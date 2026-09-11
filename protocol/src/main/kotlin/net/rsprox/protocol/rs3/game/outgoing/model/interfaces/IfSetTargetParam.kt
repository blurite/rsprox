package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetTargetParam(
    public val componentHash: Long,
    public val targetParam: Int,
    public val fromSlot: Int,
    public val toSlot: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetTargetParam

        if (componentHash != other.componentHash) return false
        if (targetParam != other.targetParam) return false
        if (fromSlot != other.fromSlot) return false
        if (toSlot != other.toSlot) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + targetParam
        result = 31 * result + fromSlot
        result = 31 * result + toSlot
        return result
    }

    override fun toString(): String {
        return "IfSetTargetParam(componentHash=$componentHash, targetParam=$targetParam, " +
            "fromSlot=$fromSlot, toSlot=$toSlot)"
    }
}
