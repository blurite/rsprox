package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

public class SequenceExtendedInfo(
    public val id: Int,
    public val delay: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SequenceExtendedInfo

        if (id != other.id) return false
        if (delay != other.delay) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + delay
        return result
    }

    override fun toString(): String {
        return "SequenceExtendedInfo(" +
            "id=$id, " +
            "delay=$delay" +
            ")"
    }
}
