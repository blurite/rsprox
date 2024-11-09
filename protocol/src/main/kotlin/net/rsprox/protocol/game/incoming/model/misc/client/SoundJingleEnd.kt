package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Sound jingle end packet is sent when a jingle finishes playing in the client,
 * used to resume normal music from the start again (basically informs the server
 * that it needs to reset its internal play-time counter back to zero).
 */
public class SoundJingleEnd(
    public val jingleId: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SoundJingleEnd

        return jingleId == other.jingleId
    }

    override fun hashCode(): Int = jingleId

    override fun toString(): String = "SoundJingleEnd(jingleId=$jingleId)"
}
