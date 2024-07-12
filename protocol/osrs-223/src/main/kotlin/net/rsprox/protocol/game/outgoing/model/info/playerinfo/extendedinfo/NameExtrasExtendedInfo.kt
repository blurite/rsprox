package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

public class NameExtrasExtendedInfo(
    public val beforeName: String,
    public val afterName: String,
    public val afterCombatLevel: String,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NameExtrasExtendedInfo

        if (beforeName != other.beforeName) return false
        if (afterName != other.afterName) return false
        if (afterCombatLevel != other.afterCombatLevel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = beforeName.hashCode()
        result = 31 * result + afterName.hashCode()
        result = 31 * result + afterCombatLevel.hashCode()
        return result
    }

    override fun toString(): String {
        return "NameExtrasExtendedInfo(" +
            "beforeName='$beforeName', " +
            "afterName='$afterName', " +
            "afterCombatLevel='$afterCombatLevel'" +
            ")"
    }
}
