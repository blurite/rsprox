package net.rsprox.protocol.rs3v949.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class HintTrail(
    public val slot: Int,
    public val modelId: Int,
    public val trailingBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HintTrail

        if (slot != other.slot) return false
        if (modelId != other.modelId) return false
        if (!trailingBytes.contentEquals(other.trailingBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slot
        result = 31 * result + modelId
        result = 31 * result + trailingBytes.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "HintTrail(slot=$slot, modelId=$modelId, trailingBytes=${trailingBytes.size}b)"
    }
}
