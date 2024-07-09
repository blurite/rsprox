package net.rsprox.protocol.game.incoming.model.clan

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * Clan settings requests are made whenever the server sends a clansettings
 * delta update, but the update counter in the clan settings message
 * is greater than that of the clan itself. In order to avoid problems,
 * the client requests for a full clan settings update from the server,
 * to re-synchronize all the values.
 * @property clanId the id of the clan to request, ranging from 0 to 3 (inclusive),
 * or a negative value if the request is for a guest-clan
 */
public class ClanSettingsFullRequest(
    public val clanId: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClanSettingsFullRequest

        return clanId == other.clanId
    }

    override fun hashCode(): Int {
        return clanId
    }

    override fun toString(): String {
        return "ClanSettingsFullRequest(clanId=$clanId)"
    }
}
