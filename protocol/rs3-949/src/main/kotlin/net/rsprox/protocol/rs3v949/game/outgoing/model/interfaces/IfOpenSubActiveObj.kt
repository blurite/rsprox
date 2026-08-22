package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfOpenSubActiveObj(
    public val componentHash: Long,
    public val childId: Int,
    public val objId: Int,
    public val layer: Int,
    public val extra1: Int,
    public val extra2: Int,
    public val extra3: Int,
    public val extra4: Int,
    public val trailingBytes: ByteArray,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfOpenSubActiveObj

        if (componentHash != other.componentHash) return false
        if (childId != other.childId) return false
        if (objId != other.objId) return false
        if (layer != other.layer) return false
        if (extra1 != other.extra1) return false
        if (extra2 != other.extra2) return false
        if (extra3 != other.extra3) return false
        if (extra4 != other.extra4) return false
        if (!trailingBytes.contentEquals(other.trailingBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + childId
        result = 31 * result + objId
        result = 31 * result + layer
        result = 31 * result + extra1
        result = 31 * result + extra2
        result = 31 * result + extra3
        result = 31 * result + extra4
        result = 31 * result + trailingBytes.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "IfOpenSubActiveObj(componentHash=$componentHash, childId=$childId, objId=$objId, " +
            "layer=$layer, extra1=$extra1, extra2=$extra2, extra3=$extra3, extra4=$extra4, " +
            "trailingBytes=${trailingBytes.size}b)"
    }
}
