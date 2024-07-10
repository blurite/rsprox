package net.rsprox.protocol.game.incoming.model.friendchat

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Friend chat set rank message is sent when the owner of a friend chat
 * channel changes the rank of another player who is on their friendlist.
 * @property name the name of the player whose rank to change
 * @property rank the id of the new rank to set to that player
 */
@Suppress("MemberVisibilityCanBePrivate")
public class FriendChatSetRank private constructor(
    public val name: String,
    private val _rank: UByte,
) : IncomingGameMessage {
    public constructor(
        name: String,
        rank: Int,
    ) : this(
        name,
        rank.toUByte(),
    )

    public val rank: Int
        get() = _rank.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FriendChatSetRank

        if (name != other.name) return false
        if (_rank != other._rank) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + _rank.hashCode()
        return result
    }

    override fun toString(): String {
        return "FriendChatSetRank(" +
            "name='$name', " +
            "rank=$rank" +
            ")"
    }
}
