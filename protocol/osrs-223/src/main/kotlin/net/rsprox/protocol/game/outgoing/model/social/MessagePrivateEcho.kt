package net.rsprox.protocol.game.outgoing.model.social

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Message private echo is used to show the messages
 * the given player has sent out to others,
 * in a "To name: message" format.
 * @property recipient the name of the player who received
 * the private message.
 * @property message the message to be forwarded.
 */
public class MessagePrivateEcho(
    public val recipient: String,
    public val message: String,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessagePrivateEcho

        if (recipient != other.recipient) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = recipient.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String {
        return "MessagePrivateEcho(" +
            "recipient='$recipient', " +
            "message='$message'" +
            ")"
    }
}
