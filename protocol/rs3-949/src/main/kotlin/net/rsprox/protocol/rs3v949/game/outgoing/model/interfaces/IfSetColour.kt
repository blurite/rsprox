package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetColour(
    public val componentHash: Long,
    public val packedColor: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetColour

        if (componentHash != other.componentHash) return false
        if (packedColor != other.packedColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + packedColor
        return result
    }

    override fun toString(): String {
        return "IfSetColour(componentHash=$componentHash, packedColor=$packedColor)"
    }
}
