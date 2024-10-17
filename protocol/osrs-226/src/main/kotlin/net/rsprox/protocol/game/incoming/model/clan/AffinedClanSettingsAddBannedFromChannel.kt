package net.rsprox.protocol.game.incoming.model.clan

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Clan ban messages are sent when a player with sufficient rank
 * in the clan requests to ban another member within the clan.
 * @property name the name of the player to ban
 * @property clanId the id of the clan, ranging from 0 to 3 (inclusive).
 * Negative values are not supported for bans - it is not possible to
 * ban others while you are in a clan as a guest.
 * @property memberIndex the index of the member in the clan who's being banned.
 * Note that the index isn't the player's absolute index in the world, but rather
 * the index within this clan.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class AffinedClanSettingsAddBannedFromChannel private constructor(
    public val name: String,
    private val _clanId: UByte,
    private val _memberIndex: UShort,
) : IncomingGameMessage {
    public constructor(
        name: String,
        clanId: Int,
        memberIndex: Int,
    ) : this(
        name,
        clanId.toUByte(),
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

        other as AffinedClanSettingsAddBannedFromChannel

        if (name != other.name) return false
        if (_clanId != other._clanId) return false
        if (_memberIndex != other._memberIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + _clanId.hashCode()
        result = 31 * result + _memberIndex.hashCode()
        return result
    }

    override fun toString(): String =
        "AffinedClanSettingsAddBannedFromChannel(" +
            "name='$name', " +
            "clanId=$clanId, " +
            "memberIndex=$memberIndex" +
            ")"
}
