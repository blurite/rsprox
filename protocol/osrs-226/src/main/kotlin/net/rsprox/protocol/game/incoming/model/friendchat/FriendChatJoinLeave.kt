package net.rsprox.protocol.game.incoming.model.friendchat

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Friend chat join-leave message is sent when the player joins or leaves
 * a friend chat channel.
 * @property name the name of the player whose friend chat channel to join,
 * or null if the player is leaving a friend chat channel
 */
public class FriendChatJoinLeave(
    public val name: String?,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FriendChatJoinLeave

        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "FriendChatJoinLeave(name='$name')"
}
