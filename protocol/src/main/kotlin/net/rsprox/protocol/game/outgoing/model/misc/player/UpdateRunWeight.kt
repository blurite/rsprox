package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update runweight packet is used to modify the player's current
 * equipment and inventory weight, in grams.
 */
public class UpdateRunWeight(
    public val runweight: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateRunWeight

        return runweight == other.runweight
    }

    override fun hashCode(): Int {
        return runweight
    }

    override fun toString(): String {
        return "UpdateRunWeight(runweight=$runweight)"
    }
}
