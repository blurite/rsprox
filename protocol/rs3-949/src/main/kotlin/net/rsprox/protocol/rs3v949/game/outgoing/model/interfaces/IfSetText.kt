package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetText(
    public val componentHash: Long,
    public val text: String,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetText

        if (componentHash != other.componentHash) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetText(componentHash=$componentHash, text=\"$text\")"
    }
}
