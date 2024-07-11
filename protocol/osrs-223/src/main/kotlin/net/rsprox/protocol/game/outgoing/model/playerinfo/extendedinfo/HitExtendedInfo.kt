package net.rsprox.protocol.game.outgoing.model.playerinfo.extendedinfo

public class HitExtendedInfo(
    public val hits: List<Hit>,
    public val headbars: List<Headbar>,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HitExtendedInfo

        if (hits != other.hits) return false
        if (headbars != other.headbars) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hits.hashCode()
        result = 31 * result + headbars.hashCode()
        return result
    }

    override fun toString(): String {
        return "HitExtendedInfo(" +
            "hits=$hits, " +
            "headbars=$headbars" +
            ")"
    }
}
