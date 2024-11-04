package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class AppearanceExtendedInfo(
    public val name: String,
    public val combatLevel: Int,
    public val skillLevel: Int,
    public val hidden: Boolean,
    public val gender: Int,
    public val textGender: Int,
    public val skullIcon: Int,
    public val overheadIcon: Int,
    public val transformedNpcId: Int,
    public val identKit: IntArray,
    public val interfaceIdentKit: IntArray,
    public val colours: IntArray,
    public val readyAnim: Int,
    public val turnAnim: Int,
    public val walkAnim: Int,
    public val walkAnimBack: Int,
    public val walkAnimLeft: Int,
    public val walkAnimRight: Int,
    public val runAnim: Int,
    public val beforeName: String,
    public val afterName: String,
    public val afterCombatLevel: String,
    public val forceModelRefresh: Boolean,
    public val objTypeCustomisation: Array<ObjTypeCustomisation?>?,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppearanceExtendedInfo

        if (name != other.name) return false
        if (combatLevel != other.combatLevel) return false
        if (skillLevel != other.skillLevel) return false
        if (hidden != other.hidden) return false
        if (gender != other.gender) return false
        if (textGender != other.textGender) return false
        if (skullIcon != other.skullIcon) return false
        if (overheadIcon != other.overheadIcon) return false
        if (transformedNpcId != other.transformedNpcId) return false
        if (!identKit.contentEquals(other.identKit)) return false
        if (!interfaceIdentKit.contentEquals(other.interfaceIdentKit)) return false
        if (!colours.contentEquals(other.colours)) return false
        if (readyAnim != other.readyAnim) return false
        if (turnAnim != other.turnAnim) return false
        if (walkAnim != other.walkAnim) return false
        if (walkAnimBack != other.walkAnimBack) return false
        if (walkAnimLeft != other.walkAnimLeft) return false
        if (walkAnimRight != other.walkAnimRight) return false
        if (runAnim != other.runAnim) return false
        if (beforeName != other.beforeName) return false
        if (afterName != other.afterName) return false
        if (afterCombatLevel != other.afterCombatLevel) return false
        if (forceModelRefresh != other.forceModelRefresh) return false
        if (objTypeCustomisation != null) {
            if (other.objTypeCustomisation == null) return false
            if (!objTypeCustomisation.contentEquals(other.objTypeCustomisation)) return false
        } else if (other.objTypeCustomisation != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + combatLevel
        result = 31 * result + skillLevel
        result = 31 * result + hidden.hashCode()
        result = 31 * result + gender
        result = 31 * result + textGender
        result = 31 * result + skullIcon
        result = 31 * result + overheadIcon
        result = 31 * result + transformedNpcId
        result = 31 * result + identKit.contentHashCode()
        result = 31 * result + interfaceIdentKit.contentHashCode()
        result = 31 * result + colours.contentHashCode()
        result = 31 * result + readyAnim
        result = 31 * result + turnAnim
        result = 31 * result + walkAnim
        result = 31 * result + walkAnimBack
        result = 31 * result + walkAnimLeft
        result = 31 * result + walkAnimRight
        result = 31 * result + runAnim
        result = 31 * result + beforeName.hashCode()
        result = 31 * result + afterName.hashCode()
        result = 31 * result + afterCombatLevel.hashCode()
        result = 31 * result + forceModelRefresh.hashCode()
        result = 31 * result + (objTypeCustomisation?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "AppearanceExtendedInfo(" +
            "name='$name', " +
            "combatLevel=$combatLevel, " +
            "skillLevel=$skillLevel, " +
            "hidden=$hidden, " +
            "gender=$gender, " +
            "textGender=$textGender, " +
            "skullIcon=$skullIcon, " +
            "overheadIcon=$overheadIcon, " +
            "transformedNpcId=$transformedNpcId, " +
            "identKit=${identKit.contentToString()}, " +
            "interfaceIdentKit=${interfaceIdentKit.contentToString()}, " +
            "colours=${colours.contentToString()}, " +
            "readyAnim=$readyAnim, " +
            "turnAnim=$turnAnim, " +
            "walkAnim=$walkAnim, " +
            "walkAnimBack=$walkAnimBack, " +
            "walkAnimLeft=$walkAnimLeft, " +
            "walkAnimRight=$walkAnimRight, " +
            "runAnim=$runAnim, " +
            "beforeName='$beforeName', " +
            "afterName='$afterName', " +
            "afterCombatLevel='$afterCombatLevel', " +
            "forceModelRefresh=$forceModelRefresh, " +
            "objTypeCustomisation=${objTypeCustomisation?.contentToString()}" +
            ")"
    }
}
