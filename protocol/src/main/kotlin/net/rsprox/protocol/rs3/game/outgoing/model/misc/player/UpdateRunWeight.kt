package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UpdateRunWeight(
    public val weight: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateRunWeight

        return weight == other.weight
    }

    override fun hashCode(): Int {
        return weight
    }

    override fun toString(): String {
        return "UpdateRunWeight(weight=$weight)"
    }
}
