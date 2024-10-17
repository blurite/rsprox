package net.rsprox.protocol.game.incoming.model.social

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Friend list deletion messages are sent whenever the player
 * requests to delete another user from their friend list.
 * @property name the name of the player to delete
 */
public class FriendListDel(
    public val name: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FriendListDel

        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "FriendListDel(name='$name')"
}
