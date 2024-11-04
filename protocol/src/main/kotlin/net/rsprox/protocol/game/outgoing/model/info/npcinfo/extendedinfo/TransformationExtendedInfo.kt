package net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class TransformationExtendedInfo(
    public val id: Int,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransformationExtendedInfo

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "TransformationExtendedInfo(id=$id)"
    }
}
