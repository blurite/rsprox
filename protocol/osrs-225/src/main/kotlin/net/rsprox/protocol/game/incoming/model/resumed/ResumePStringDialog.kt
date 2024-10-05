package net.rsprox.protocol.game.incoming.model.resumed

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * String dialogs are sent whenever a player enters a string into
 * the input box, e.g. for wiki search or diango's item code service.
 * @property string the string entered
 */
public class ResumePStringDialog(
    public val string: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResumePStringDialog

        return string == other.string
    }

    override fun hashCode(): Int {
        return string.hashCode()
    }

    override fun toString(): String {
        return "ResumePStringDialog(string='$string')"
    }
}
