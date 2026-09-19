package net.rsprox.protocol.rs3.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class CamMoveTo(
    public val localX: Int,
    public val localZ: Int,
    public val height: Int,
    public val speed: Int,
    public val accel: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamMoveTo

        if (localX != other.localX) return false
        if (localZ != other.localZ) return false
        if (height != other.height) return false
        if (speed != other.speed) return false
        if (accel != other.accel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = localX
        result = 31 * result + localZ
        result = 31 * result + height
        result = 31 * result + speed
        result = 31 * result + accel
        return result
    }

    override fun toString(): String {
        return "CamMoveTo(localX=$localX, localZ=$localZ, height=$height, speed=$speed, accel=$accel)"
    }
}
