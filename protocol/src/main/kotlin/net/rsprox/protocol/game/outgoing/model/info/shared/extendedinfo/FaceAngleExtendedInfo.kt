package net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo

public class FaceAngleExtendedInfo(
    public val angle: Int,
    public val instant: Boolean = false,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceAngleExtendedInfo

        if (angle != other.angle) return false
        if (instant != other.instant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = angle
        result = 31 * result + instant.hashCode()
        return result
    }

    override fun toString(): String {
        return "FaceAngleExtendedInfo(" +
            "angle=$angle, " +
            "instant=$instant" +
            ")"
    }
}
