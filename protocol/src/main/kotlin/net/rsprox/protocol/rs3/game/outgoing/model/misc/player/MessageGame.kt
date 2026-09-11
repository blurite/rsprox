package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MessageGame(
    public val type: Int,
    public val effectFlags: Int,
    public val name: String?,
    public val message: String,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageGame

        if (type != other.type) return false
        if (effectFlags != other.effectFlags) return false
        if (name != other.name) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + effectFlags
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String {
        return "MessageGame(" +
            "type=$type, " +
            "effectFlags=$effectFlags, " +
            "name=$name, " +
            "message='$message'" +
            ")"
    }
}
