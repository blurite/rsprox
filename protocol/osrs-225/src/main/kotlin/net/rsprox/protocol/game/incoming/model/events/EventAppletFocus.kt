package net.rsprox.protocol.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Applet focus events are sent whenever the client either loses or gains focus.
 * This can be seen by minimizing and maximizing the clients.
 * @property inFocus whether the client was put into focus or out of focus
 */
public class EventAppletFocus(
    public val inFocus: Boolean,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventAppletFocus

        return inFocus == other.inFocus
    }

    override fun hashCode(): Int {
        return inFocus.hashCode()
    }

    override fun toString(): String {
        return "EventAppletFocus(inFocus=$inFocus)"
    }
}
