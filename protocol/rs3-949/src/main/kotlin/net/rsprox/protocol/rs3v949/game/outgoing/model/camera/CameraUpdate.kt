package net.rsprox.protocol.rs3v949.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class CameraUpdate(
    public val headerFlags: Int,
    public val bitmask: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CameraUpdate

        if (headerFlags != other.headerFlags) return false
        if (bitmask != other.bitmask) return false

        return true
    }

    override fun hashCode(): Int {
        var result = headerFlags
        result = 31 * result + bitmask
        return result
    }

    override fun toString(): String {
        return "CameraUpdate(headerFlags=$headerFlags, bitmask=$bitmask)"
    }
}
