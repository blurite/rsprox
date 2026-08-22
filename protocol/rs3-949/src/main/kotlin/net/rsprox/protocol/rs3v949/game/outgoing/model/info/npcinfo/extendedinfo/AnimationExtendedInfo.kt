package net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.extendedinfo

public class AnimationExtendedInfo(
    public val animId: Int,
    public val speed: Int,
) : NpcExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimationExtendedInfo

        if (animId != other.animId) return false
        if (speed != other.speed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = animId
        result = 31 * result + speed
        return result
    }

    override fun toString(): String = "AnimationExtendedInfo(animId=$animId, speed=$speed)"
}
