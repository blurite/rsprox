package net.rsprox.protocol.game.incoming.model.worldentities

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * OpWorldEntity6 message is fired when a player clicks the 'Examine' option on a worldentity.
 * @property id the config id of the worldentity clicked
 */
public class OpWorldEntity6(
    public val id: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpWorldEntity6

        return id == other.id
    }

    override fun hashCode(): Int = id

    override fun toString(): String = "OpWorldEntity6(id=$id)"
}
