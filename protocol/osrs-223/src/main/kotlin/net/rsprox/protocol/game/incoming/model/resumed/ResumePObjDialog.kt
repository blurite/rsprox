package net.rsprox.protocol.game.incoming.model.resumed

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * Resume p obj dialogue is sent when the user selects an obj from the
 * Grand Exchange item search box, however this packet is not necessarily
 * exclusive to that feature, and can be used in other pieces of content.
 * @property obj the id of the obj selected
 */
public class ResumePObjDialog(
    public val obj: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResumePObjDialog

        return obj == other.obj
    }

    override fun hashCode(): Int {
        return obj
    }

    override fun toString(): String {
        return "ResumePObjDialog(obj=$obj)"
    }
}
