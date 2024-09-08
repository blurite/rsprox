package net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class HeadIconCustomisationExtendedInfo(
    public val groups: IntArray,
    public val indices: IntArray,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeadIconCustomisationExtendedInfo

        if (!groups.contentEquals(other.groups)) return false
        if (!indices.contentEquals(other.indices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groups.contentHashCode()
        result = 31 * result + indices.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "HeadIconCustomisationExtendedInfo(" +
            "groups=${groups.contentToString()}, " +
            "indices=${indices.contentToString()}" +
            ")"
    }
}
