package net.rsprox.protocol.game.incoming.model.resumed

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Resume p count dialogue is sent whenever a player enters an
 * integer to the input box, e.g. to withdraw an item in x-quantity.
 * @property count the count entered. While this can only be a positive
 * integer for manually-entered inputs, it is **not** guaranteed to always
 * be positive. Clientscripts can invoke this event with negative values to
 * represent various potential response codes.
 */
public class ResumePCountDialog(
    public val count: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResumePCountDialog

        return count == other.count
    }

    override fun hashCode(): Int {
        return count
    }

    override fun toString(): String {
        return "ResumePCountDialog(count=$count)"
    }
}
