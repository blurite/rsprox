package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Set heading packet is used to update the current heading/angle/direction of the worldentity.
 * @property heading the heading in which to turn the worldentity. A value of 0-15 (inclusive).
 * This value is a scaled down variant of the 0-2048 angle that is normally used, except the value is
 * divided by 128.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class SetHeading(
    public val heading: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SetHeading) return false

        if (heading != other.heading) return false

        return true
    }

    override fun hashCode(): Int {
        return heading
    }

    override fun toString(): String {
        return "SetHeading(" +
            "heading=$heading" +
            ")"
    }
}
