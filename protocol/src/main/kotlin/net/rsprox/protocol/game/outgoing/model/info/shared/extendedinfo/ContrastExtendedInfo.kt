package net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo

public class ContrastExtendedInfo(
    public val start: Int,
    public val end: Int,
    public val startContrast: Int,
    public val endContrast: Int,
    public val useStartContrast: Boolean,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContrastExtendedInfo

        if (start != other.start) return false
        if (end != other.end) return false
        if (startContrast != other.startContrast) return false
        if (endContrast != other.endContrast) return false
        if (useStartContrast != other.useStartContrast) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + end
        result = 31 * result + startContrast
        result = 31 * result + endContrast
        result = 31 * result + useStartContrast.hashCode()
        return result
    }

    override fun toString(): String {
        return "ContrastExtendedInfo(" +
            "start=$start, " +
            "end=$end, " +
            "startContrast=$startContrast, " +
            "endContrast=$endContrast, " +
            "useStartContrast=$useStartContrast" +
            ")"
    }
}