package net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfOpenTop(
    public val interfaceId: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfOpenTop

        return interfaceId == other.interfaceId
    }

    override fun hashCode(): Int {
        return interfaceId
    }

    override fun toString(): String {
        return "IfOpenTop(interfaceId=$interfaceId)"
    }
}
