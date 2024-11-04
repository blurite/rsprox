package net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class NameChangeExtendedInfo(
    public val name: String,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NameChangeExtendedInfo

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "NameChangeExtendedInfo(name='$name')"
    }
}
