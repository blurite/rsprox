package net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class BaseAnimationSetExtendedInfo(
    public val turnLeftAnim: Int?,
    public val turnRightAnim: Int?,
    public val walkAnim: Int?,
    public val walkAnimBack: Int?,
    public val walkAnimLeft: Int?,
    public val walkAnimRight: Int?,
    public val runAnim: Int?,
    public val runAnimBack: Int?,
    public val runAnimLeft: Int?,
    public val runAnimRight: Int?,
    public val crawlAnim: Int?,
    public val crawlAnimBack: Int?,
    public val crawlAnimLeft: Int?,
    public val crawlAnimRight: Int?,
    public val readyAnim: Int?,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseAnimationSetExtendedInfo

        if (turnLeftAnim != other.turnLeftAnim) return false
        if (turnRightAnim != other.turnRightAnim) return false
        if (walkAnim != other.walkAnim) return false
        if (walkAnimBack != other.walkAnimBack) return false
        if (walkAnimLeft != other.walkAnimLeft) return false
        if (walkAnimRight != other.walkAnimRight) return false
        if (runAnim != other.runAnim) return false
        if (runAnimBack != other.runAnimBack) return false
        if (runAnimLeft != other.runAnimLeft) return false
        if (runAnimRight != other.runAnimRight) return false
        if (crawlAnim != other.crawlAnim) return false
        if (crawlAnimBack != other.crawlAnimBack) return false
        if (crawlAnimLeft != other.crawlAnimLeft) return false
        if (crawlAnimRight != other.crawlAnimRight) return false
        if (readyAnim != other.readyAnim) return false

        return true
    }

    override fun hashCode(): Int {
        var result = turnLeftAnim ?: 0
        result = 31 * result + (turnRightAnim ?: 0)
        result = 31 * result + (walkAnim ?: 0)
        result = 31 * result + (walkAnimBack ?: 0)
        result = 31 * result + (walkAnimLeft ?: 0)
        result = 31 * result + (walkAnimRight ?: 0)
        result = 31 * result + (runAnim ?: 0)
        result = 31 * result + (runAnimBack ?: 0)
        result = 31 * result + (runAnimLeft ?: 0)
        result = 31 * result + (runAnimRight ?: 0)
        result = 31 * result + (crawlAnim ?: 0)
        result = 31 * result + (crawlAnimBack ?: 0)
        result = 31 * result + (crawlAnimLeft ?: 0)
        result = 31 * result + (crawlAnimRight ?: 0)
        result = 31 * result + (readyAnim ?: 0)
        return result
    }

    override fun toString(): String {
        return "BaseAnimationSetExtendedInfo(" +
            "turnLeftAnim=$turnLeftAnim, " +
            "turnRightAnim=$turnRightAnim, " +
            "walkAnim=$walkAnim, " +
            "walkAnimBack=$walkAnimBack, " +
            "walkAnimLeft=$walkAnimLeft, " +
            "walkAnimRight=$walkAnimRight, " +
            "runAnim=$runAnim, " +
            "runAnimBack=$runAnimBack, " +
            "runAnimLeft=$runAnimLeft, " +
            "runAnimRight=$runAnimRight, " +
            "crawlAnim=$crawlAnim, " +
            "crawlAnimBack=$crawlAnimBack, " +
            "crawlAnimLeft=$crawlAnimLeft, " +
            "crawlAnimRight=$crawlAnimRight, " +
            "readyAnim=$readyAnim" +
            ")"
    }
}
