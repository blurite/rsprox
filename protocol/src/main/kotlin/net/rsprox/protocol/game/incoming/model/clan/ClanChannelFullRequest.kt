package net.rsprox.protocol.game.incoming.model.clan

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Clan channel requests are made whenever the server sends a clanchannel
 * delta update, but the client does not have a clan defined at that id.
 * In order to fix the problem, the client will then request for a full
 * clan update for that clan id.
 * @property clanId the id of the clan to request, ranging from 0 to 3 (inclusive),
 * or a negative value if the request is for a guest-clan
 */
public class ClanChannelFullRequest(
    public val clanId: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClanChannelFullRequest

        return clanId == other.clanId
    }

    override fun hashCode(): Int = clanId

    override fun toString(): String = "ClanChannelFullRequest(clanId=$clanId)"
}
