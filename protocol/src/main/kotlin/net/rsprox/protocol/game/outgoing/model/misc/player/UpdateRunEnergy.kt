package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update runenergy packet is used to modify the player's current
 * run energy. 100 units equals one percentage on the run orb,
 * meaning a value of 10,000 is equal to 100% run energy.
 */
public class UpdateRunEnergy(
    public val runenergy: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateRunEnergy

        return runenergy == other.runenergy
    }

    override fun hashCode(): Int {
        return runenergy
    }

    override fun toString(): String {
        return "UpdateRunEnergy(runenergy=$runenergy)"
    }
}
