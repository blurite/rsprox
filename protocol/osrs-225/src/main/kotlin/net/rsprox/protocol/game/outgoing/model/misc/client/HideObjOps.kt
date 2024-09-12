package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Hide obj ops packet is used to hide the right-click menu of all objs on the ground.
 * @property hidden whether to hide all the click options of objs.
 */
public class HideObjOps(
    public val hidden: Boolean,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HideObjOps

        return hidden == other.hidden
    }

    override fun hashCode(): Int = hidden.hashCode()

    override fun toString(): String = "HideObjOps(hidden=$hidden)"
}
