package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class HintArrow(
    public val slot: Int,
    public val type: Int,
    public val targetIndex: Int?,
    public val x: Int?,
    public val y: Int?,
    public val z: Int?,
    public val distance: Int?,
    public val trailingBytes: ByteArray,
) : IncomingServerGameMessage {
    public val isReset: Boolean
        get() = type == 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HintArrow

        if (slot != other.slot) return false
        if (type != other.type) return false
        if (targetIndex != other.targetIndex) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (distance != other.distance) return false
        if (!trailingBytes.contentEquals(other.trailingBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slot
        result = 31 * result + type
        result = 31 * result + (targetIndex ?: 0)
        result = 31 * result + (x ?: 0)
        result = 31 * result + (y ?: 0)
        result = 31 * result + (z ?: 0)
        result = 31 * result + (distance ?: 0)
        result = 31 * result + trailingBytes.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "HintArrow(slot=$slot, type=$type, targetIndex=$targetIndex, x=$x, y=$y, z=$z, " +
            "distance=$distance, trailingBytes=${trailingBytes.size}b)"
    }
}
