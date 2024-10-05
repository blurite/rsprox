package net.rsprox.protocol.game.incoming.model.locs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * OpLoc6 message is fired whenever a player clicks examine on a loc.
 * @property id the id of the loc (if multiloc, transformed to the
 * currently visible variant)
 */
public class OpLoc6(
    public val id: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpLoc6

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "OpLoc6(id=$id)"
    }
}
