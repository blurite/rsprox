package net.rsprox.protocol.rs3v949.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class CamForceAngle(
    public val yaw: Int,
    public val pitch: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamForceAngle

        if (yaw != other.yaw) return false
        if (pitch != other.pitch) return false

        return true
    }

    override fun hashCode(): Int {
        var result = yaw
        result = 31 * result + pitch
        return result
    }

    override fun toString(): String {
        return "CamForceAngle(yaw=$yaw, pitch=$pitch)"
    }
}
