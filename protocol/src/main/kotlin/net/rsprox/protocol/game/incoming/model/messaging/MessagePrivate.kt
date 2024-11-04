package net.rsprox.protocol.game.incoming.model.messaging

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Message private events are sent when a player writes a private
 * message to the target player. The server is responsible for looking
 * up the target player and forwarding the message to them, if possible.
 * @property name the name of the recipient of this private message
 * @property message the message forwarded to the recipient
 */
public class MessagePrivate(
    public val name: String,
    public val message: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessagePrivate

        if (name != other.name) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String =
        "MessagePrivate(" +
            "name='$name', " +
            "message='$message'" +
            ")"
}
