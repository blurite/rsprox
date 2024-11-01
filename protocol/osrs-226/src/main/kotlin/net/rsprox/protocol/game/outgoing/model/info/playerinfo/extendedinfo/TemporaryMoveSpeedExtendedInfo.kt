package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class TemporaryMoveSpeedExtendedInfo(
    public val speed: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TemporaryMoveSpeedExtendedInfo

        return speed == other.speed
    }

    override fun hashCode(): Int {
        return speed
    }

    override fun toString(): String {
        return "TemporaryMoveSpeedExtendedInfo(speed=$speed)"
    }
}
