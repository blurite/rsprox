package net.rsprox.protocol.game.incoming.model.social

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Ignore list deletion messages are sent whenever the player
 * requests to delete another player from their ignorelist
 * @property name the name of the player to delete from their ignorelist
 */
public class IgnoreListDel(
    public val name: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IgnoreListDel

        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "IgnoreListDel(name='$name')"
}
