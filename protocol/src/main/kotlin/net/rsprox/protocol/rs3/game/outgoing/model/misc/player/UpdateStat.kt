package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UpdateStat(
    public val skillId: Int,
    public val level: Int,
    public val xp: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateStat

        if (skillId != other.skillId) return false
        if (level != other.level) return false
        if (xp != other.xp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = skillId
        result = 31 * result + level
        result = 31 * result + xp
        return result
    }

    override fun toString(): String {
        return "UpdateStat(skillId=$skillId, level=$level, xp=$xp)"
    }
}
