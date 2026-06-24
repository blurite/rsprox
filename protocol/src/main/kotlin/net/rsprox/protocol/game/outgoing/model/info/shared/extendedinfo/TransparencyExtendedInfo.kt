package net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo

public class TransparencyExtendedInfo(
    public val start: Int,
    public val end: Int,
    public val startTransparency: Int,
    public val endTransparency: Int,
    public val useStartTransparency: Boolean,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransparencyExtendedInfo

        if (start != other.start) return false
        if (end != other.end) return false
        if (startTransparency != other.startTransparency) return false
        if (endTransparency != other.endTransparency) return false
        if (useStartTransparency != other.useStartTransparency) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + end
        result = 31 * result + startTransparency
        result = 31 * result + endTransparency
        result = 31 * result + useStartTransparency.hashCode()
        return result
    }

    override fun toString(): String {
        return "TransparencyExtendedInfo(" +
            "start=$start, " +
            "end=$end, " +
            "startTransparency=$startTransparency, " +
            "endTransparency=$endTransparency, " +
            "useStartTransparency=$useStartTransparency" +
            ")"
    }
}
