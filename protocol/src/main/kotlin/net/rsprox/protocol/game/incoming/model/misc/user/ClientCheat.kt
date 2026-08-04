package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Client cheats are commands sent in chat using the :: prefix,
 * or through the console on the C++ client.
 */
public class ClientCheat(
    public val command: String,
    public val unknown: Int? = null,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientCheat

        if (unknown != other.unknown) return false
        if (command != other.command) return false

        return true
    }

    override fun hashCode(): Int {
        var result = unknown.hashCode()
        result = 31 * result + command.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClientCheat(" +
            "command='$command', " +
            "unknown=$unknown" +
            ")"
    }
}
