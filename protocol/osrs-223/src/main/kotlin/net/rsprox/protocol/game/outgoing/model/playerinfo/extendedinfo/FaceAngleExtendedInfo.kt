package net.rsprox.protocol.game.outgoing.model.playerinfo.extendedinfo

public class FaceAngleExtendedInfo(
    public val angle: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceAngleExtendedInfo

        return angle == other.angle
    }

    override fun hashCode(): Int {
        return angle
    }

    override fun toString(): String {
        return "FaceAngleExtendedInfo(angle=$angle)"
    }
}
