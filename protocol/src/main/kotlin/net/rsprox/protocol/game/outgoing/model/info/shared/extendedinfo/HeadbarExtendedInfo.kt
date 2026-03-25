package net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo

public class HeadbarExtendedInfo(
    public val headbars: List<Headbar>,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeadbarExtendedInfo

        if (headbars != other.headbars) return false

        return true
    }

    override fun hashCode(): Int {
        return headbars.hashCode()
    }

    override fun toString(): String {
        return "HitExtendedInfo(" +
            "headbars=$headbars" +
            ")"
    }
}
