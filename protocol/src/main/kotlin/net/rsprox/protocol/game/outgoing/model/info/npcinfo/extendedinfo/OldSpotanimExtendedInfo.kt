package net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class OldSpotanimExtendedInfo(
    public val id: Int,
    public val delay: Int,
    public val height: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OldSpotanimExtendedInfo

        if (id != other.id) return false
        if (delay != other.delay) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + delay
        result = 31 * result + height
        return result
    }

    override fun toString(): String {
        return "OldSpotanimExtendedInfo(" +
            "id=$id, " +
            "delay=$delay, " +
            "height=$height" +
            ")"
    }
}
