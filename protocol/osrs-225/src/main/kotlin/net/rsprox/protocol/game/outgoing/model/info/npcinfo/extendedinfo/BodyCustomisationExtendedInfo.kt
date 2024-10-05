package net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo

import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.customisation.CustomisationType
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public class BodyCustomisationExtendedInfo(
    public val type: CustomisationType,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BodyCustomisationExtendedInfo

        return type == other.type
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun toString(): String {
        return "BodyCustomisationExtendedInfo(type=$type)"
    }
}
