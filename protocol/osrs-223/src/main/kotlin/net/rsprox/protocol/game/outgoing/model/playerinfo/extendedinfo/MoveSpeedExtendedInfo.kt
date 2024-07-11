package net.rsprox.protocol.game.outgoing.model.playerinfo.extendedinfo

public class MoveSpeedExtendedInfo(
    public val speed: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MoveSpeedExtendedInfo

        return speed == other.speed
    }

    override fun hashCode(): Int {
        return speed
    }

    override fun toString(): String {
        return "MoveSpeedExtendedInfo(speed=$speed)"
    }
}
