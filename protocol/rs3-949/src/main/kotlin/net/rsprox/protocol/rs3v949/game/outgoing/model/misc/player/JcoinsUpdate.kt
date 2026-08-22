package net.rsprox.protocol.rs3v949.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class JcoinsUpdate(
    public val jcoins: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JcoinsUpdate

        return jcoins == other.jcoins
    }

    override fun hashCode(): Int {
        return jcoins
    }

    override fun toString(): String {
        return "JcoinsUpdate(jcoins=$jcoins)"
    }
}
