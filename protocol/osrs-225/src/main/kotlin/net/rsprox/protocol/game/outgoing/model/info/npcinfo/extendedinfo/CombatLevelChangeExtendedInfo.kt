package net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class CombatLevelChangeExtendedInfo(
    public val level: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CombatLevelChangeExtendedInfo

        return level == other.level
    }

    override fun hashCode(): Int {
        return level
    }

    override fun toString(): String {
        return "CombatLevelChangeExtendedInfo(level=$level)"
    }
}
