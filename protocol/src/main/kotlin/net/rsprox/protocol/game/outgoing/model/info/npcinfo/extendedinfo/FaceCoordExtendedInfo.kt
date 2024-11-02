package net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class FaceCoordExtendedInfo(
    public val x: Int,
    public val z: Int,
    public val instant: Boolean,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceCoordExtendedInfo

        if (x != other.x) return false
        if (z != other.z) return false
        if (instant != other.instant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + z
        result = 31 * result + instant.hashCode()
        return result
    }

    override fun toString(): String {
        return "FaceCoordExtendedInfo(" +
            "x=$x, " +
            "z=$z, " +
            "instant=$instant" +
            ")"
    }
}
