package net.rsprox.protocol.game.incoming.model.npcs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * OpNpc6 message is fired when a player clicks the 'Examine' option on a npc.
 * @property id the config id of the npc clicked
 */
public class OpNpc6(
    public val id: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpNpc6

        return id == other.id
    }

    override fun hashCode(): Int = id

    override fun toString(): String = "OpNpc6(id=$id)"
}
