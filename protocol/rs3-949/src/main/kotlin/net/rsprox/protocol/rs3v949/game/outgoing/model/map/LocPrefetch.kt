package net.rsprox.protocol.rs3v949.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class LocPrefetch(
    public val locId: Int,
    public val shapeRot: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocPrefetch

        if (locId != other.locId) return false
        if (shapeRot != other.shapeRot) return false

        return true
    }

    override fun hashCode(): Int {
        var result = locId
        result = 31 * result + shapeRot
        return result
    }

    override fun toString(): String {
        return "LocPrefetch(locId=$locId, shapeRot=$shapeRot)"
    }
}
