package net.rsprox.protocol.rs3.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class CamShake(
    public val shakeMode: Int,
    public val param0: Int,
    public val param1: Int,
    public val param2: Int,
    public val param3: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamShake

        if (shakeMode != other.shakeMode) return false
        if (param0 != other.param0) return false
        if (param1 != other.param1) return false
        if (param2 != other.param2) return false
        if (param3 != other.param3) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shakeMode
        result = 31 * result + param0
        result = 31 * result + param1
        result = 31 * result + param2
        result = 31 * result + param3
        return result
    }

    override fun toString(): String {
        return "CamShake(shakeMode=$shakeMode, param0=$param0, param1=$param1, param2=$param2, param3=$param3)"
    }
}
