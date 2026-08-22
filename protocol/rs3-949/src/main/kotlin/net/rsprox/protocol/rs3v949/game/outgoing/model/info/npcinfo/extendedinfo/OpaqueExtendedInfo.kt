package net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.extendedinfo

public class OpaqueExtendedInfo(
    public val rendered: String,
) : NpcExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpaqueExtendedInfo

        return rendered == other.rendered
    }

    override fun hashCode(): Int = rendered.hashCode()

    override fun toString(): String = rendered
}
