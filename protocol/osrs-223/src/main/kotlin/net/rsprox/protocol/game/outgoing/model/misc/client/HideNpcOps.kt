package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Hide npc ops packet is used to hide the right-click menu of all NPCs across the game.
 * @property hidden whether to hide all the click options of NPCs.
 */
public class HideNpcOps(
    public val hidden: Boolean,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HideNpcOps

        return hidden == other.hidden
    }

    override fun hashCode(): Int = hidden.hashCode()

    override fun toString(): String = "HideNpcOps(hidden=$hidden)"
}
