package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

public class TintingExtendedInfo(
    public val start: Int,
    public val end: Int,
    public val hue: Int,
    public val saturation: Int,
    public val lightness: Int,
    public val weight: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TintingExtendedInfo

        if (start != other.start) return false
        if (end != other.end) return false
        if (hue != other.hue) return false
        if (saturation != other.saturation) return false
        if (lightness != other.lightness) return false
        if (weight != other.weight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + end
        result = 31 * result + hue
        result = 31 * result + saturation
        result = 31 * result + lightness
        result = 31 * result + weight
        return result
    }

    override fun toString(): String {
        return "TintingExtendedInfo(" +
            "start=$start, " +
            "end=$end, " +
            "hue=$hue, " +
            "saturation=$saturation, " +
            "lightness=$lightness, " +
            "weight=$weight" +
            ")"
    }
}
