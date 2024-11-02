package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Detect modified client is sent by the client right before a map load
 * if the client has been given a frame. For simple deobs, this is generally
 * not the case.
 * In OSRS, the code is consistently sent as '1,057,001,181'.
 */
public class DetectModifiedClient(
    public val code: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetectModifiedClient

        return code == other.code
    }

    override fun hashCode(): Int = code

    override fun toString(): String = "DetectModifiedClient(code=$code)"
}
