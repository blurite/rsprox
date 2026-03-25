package net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo

public class HitmarkExtendedInfo(
    public val hits: List<Hit>,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HitmarkExtendedInfo

        if (hits != other.hits) return false

        return true
    }

    override fun hashCode(): Int {
        return hits.hashCode()
    }

    override fun toString(): String {
        return "HitExtendedInfo(" +
            "hits=$hits" +
            ")"
    }
}
