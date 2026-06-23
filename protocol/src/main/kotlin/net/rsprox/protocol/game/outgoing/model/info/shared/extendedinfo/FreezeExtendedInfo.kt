package net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo

public class FreezeExtendedInfo(
    public val delay: Int,
    public val duration: Int,
    public val cancelSequence: Boolean,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FreezeExtendedInfo

        if (delay != other.delay) return false
        if (duration != other.duration) return false
        if (cancelSequence != other.cancelSequence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = delay
        result = 31 * result + duration
        result = 31 * result + cancelSequence.hashCode()
        return result
    }

    override fun toString(): String {
        return "FreezeExtendedInfo(" +
            "delay=$delay, " +
            "duration=$duration, " +
            "cancelSequence=$cancelSequence" +
            ")"
    }
}