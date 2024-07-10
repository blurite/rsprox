package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Heatmap toggle packet is used to either enable or
 * disabled the heatmap, which is rendered over the
 * world map in OldSchool.
 * This packet utilizes high resolution coordinate info
 * about all the players of the game through player info
 * packet, so in order for it to properly function,
 * high resolution information must be sent for everyone
 * in the game.
 */
public class HeatmapToggle(
    public val enabled: Boolean,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeatmapToggle

        return enabled == other.enabled
    }

    override fun hashCode(): Int {
        return enabled.hashCode()
    }

    override fun toString(): String {
        return "HeatmapToggle(enabled=$enabled)"
    }
}
