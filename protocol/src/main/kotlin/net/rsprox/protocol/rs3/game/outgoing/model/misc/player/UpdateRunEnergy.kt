package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UpdateRunEnergy(
    public val energy: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateRunEnergy

        return energy == other.energy
    }

    override fun hashCode(): Int {
        return energy
    }

    override fun toString(): String {
        return "UpdateRunEnergy(energy=$energy)"
    }
}
