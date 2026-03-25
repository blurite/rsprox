package net.rsprox.protocol.game.incoming.model.resumed

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Resume p count dialogue is sent whenever a player enters a
 * long to the input box. It has no uses as of writing this.
 * @property count the count entered.
 */
public class ResumePCountDialogLong(
    public val count: Long,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResumePCountDialogLong

        return count == other.count
    }

    override fun hashCode(): Int = count.hashCode()

    override fun toString(): String = "ResumePCountDialogLong(count=$count)"
}
