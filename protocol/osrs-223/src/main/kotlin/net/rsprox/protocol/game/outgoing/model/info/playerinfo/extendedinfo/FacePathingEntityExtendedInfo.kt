package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

public class FacePathingEntityExtendedInfo(
    public val index: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FacePathingEntityExtendedInfo

        return index == other.index
    }

    override fun hashCode(): Int {
        return index
    }

    override fun toString(): String {
        return "FacePathingEntityExtendedInfo(index=$index)"
    }
}
