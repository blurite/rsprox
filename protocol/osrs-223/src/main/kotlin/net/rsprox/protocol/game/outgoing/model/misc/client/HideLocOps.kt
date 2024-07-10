package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Hide loc ops packet is used to hide the right-click menu of all locs across the game.
 * @property hidden whether to hide all the click options of locs.
 */
public class HideLocOps(
    public val hidden: Boolean,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HideLocOps

        return hidden == other.hidden
    }

    override fun hashCode(): Int = hidden.hashCode()

    override fun toString(): String = "HideLocOps(hidden=$hidden)"
}
