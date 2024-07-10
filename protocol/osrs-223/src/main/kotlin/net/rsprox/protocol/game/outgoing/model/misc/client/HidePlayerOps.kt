package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Hide player ops packet is used to hide the right-click menu of all players across the game.
 * @property hidden whether to hide all the click options of players.
 */
public class HidePlayerOps(
    public val hidden: Boolean,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HidePlayerOps

        return hidden == other.hidden
    }

    override fun hashCode(): Int = hidden.hashCode()

    override fun toString(): String = "HidePlayerOps(hidden=$hidden)"
}
