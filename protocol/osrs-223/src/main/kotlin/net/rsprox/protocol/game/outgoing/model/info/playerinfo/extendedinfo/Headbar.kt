package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

public class Headbar(
    public val type: Int,
    public val startFill: Int,
    public val endFill: Int,
    public val startTime: Int,
    public val endTime: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Headbar

        if (type != other.type) return false
        if (startFill != other.startFill) return false
        if (endFill != other.endFill) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + startFill
        result = 31 * result + endFill
        result = 31 * result + startTime
        result = 31 * result + endTime
        return result
    }

    override fun toString(): String {
        return "Headbar(" +
            "type=$type, " +
            "startFill=$startFill, " +
            "endFill=$endFill, " +
            "startTime=$startTime, " +
            "endTime=$endTime" +
            ")"
    }
}
