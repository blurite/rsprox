package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetPosition(
    public val componentHash: Long,
    public val x: Int,
    public val y: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetPosition

        if (componentHash != other.componentHash) return false
        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + x
        result = 31 * result + y
        return result
    }

    override fun toString(): String {
        return "IfSetPosition(componentHash=$componentHash, x=$x, y=$y)"
    }
}
