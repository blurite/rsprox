package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * Client cheats are commands sent in chat using the :: prefix,
 * or through the console on the C++ client.
 */
public class ClientCheat(
    public val command: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientCheat

        return command == other.command
    }

    override fun hashCode(): Int {
        return command.hashCode()
    }

    override fun toString(): String {
        return "ClientCheat(command='$command')"
    }
}
