package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

public class ExactMoveExtendedInfo(
    public val deltaX1: Int,
    public val deltaZ1: Int,
    public val delay1: Int,
    public val deltaX2: Int,
    public val deltaZ2: Int,
    public val delay2: Int,
    public val direction: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExactMoveExtendedInfo

        if (deltaX1 != other.deltaX1) return false
        if (deltaZ1 != other.deltaZ1) return false
        if (delay1 != other.delay1) return false
        if (deltaX2 != other.deltaX2) return false
        if (deltaZ2 != other.deltaZ2) return false
        if (delay2 != other.delay2) return false
        if (direction != other.direction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deltaX1
        result = 31 * result + deltaZ1
        result = 31 * result + delay1
        result = 31 * result + deltaX2
        result = 31 * result + deltaZ2
        result = 31 * result + delay2
        result = 31 * result + direction
        return result
    }

    override fun toString(): String {
        return "ExactMoveExtendedInfo(" +
            "deltaX1=$deltaX1, " +
            "deltaZ1=$deltaZ1, " +
            "delay1=$delay1, " +
            "deltaX2=$deltaX2, " +
            "deltaZ2=$deltaZ2, " +
            "delay2=$delay2, " +
            "direction=$direction" +
            ")"
    }
}
