package net.rsprox.protocol.rs3.game.outgoing.model.inv

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UpdateInvStopTransmit(
    public val inventoryId: Int,
    public val flags: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateInvStopTransmit

        if (inventoryId != other.inventoryId) return false
        if (flags != other.flags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inventoryId
        result = 31 * result + flags
        return result
    }

    override fun toString(): String {
        return "UpdateInvStopTransmit(inventoryId=$inventoryId, flags=$flags)"
    }
}
