package net.rsprox.protocol.game.incoming.model.clan

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Clan kick messages are sent when a player with sufficient privileges
 * requests to kick another player within the clan out of it.
 * @property name the name of the player to kick
 * @property clanId the id of the clan the player is in, ranging from 0 to 3 (inclusive),
 * or negative values if referring to a guest clan
 * @property memberIndex the index of the member in the clan who's being kicked.
 * Note that the index isn't the player's absolute index in the world, but rather
 * the index within this clan.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class ClanChannelKickUser private constructor(
    public val name: String,
    private val _clanId: Byte,
    private val _memberIndex: UShort,
) : IncomingGameMessage {
    public constructor(
        name: String,
        clanId: Int,
        memberIndex: Int,
    ) : this(
        name,
        clanId.toByte(),
        memberIndex.toUShort(),
    )

    public val clanId: Int
        get() = _clanId.toInt()
    public val memberIndex: Int
        get() = _memberIndex.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClanChannelKickUser

        if (name != other.name) return false
        if (_clanId != other._clanId) return false
        if (_memberIndex != other._memberIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + _clanId
        result = 31 * result + _memberIndex.hashCode()
        return result
    }

    override fun toString(): String =
        "ClanChannelKickUser(" +
            "name='$name', " +
            "clanId=$clanId, " +
            "memberIndex=$memberIndex" +
            ")"
}
