package net.rsprox.protocol.rs3v949.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

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

    override fun hashCode(): Int = inFocus.hashCode()

    override fun toString(): String = "EventAppletFocus(inFocus=$inFocus)"
}
