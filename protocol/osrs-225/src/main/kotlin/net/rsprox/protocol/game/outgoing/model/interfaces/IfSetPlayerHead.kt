package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * If set-player-head is used to set the local player's chathead on an interface,
 * commonly used for dialogues.
 * @property interfaceId the id of the interface on which the chathead model resides
 * @property componentId the id of the component on which the chathead model resides
 */
public class IfSetPlayerHead private constructor(
    public val combinedId: CombinedId,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetPlayerHead

        return combinedId == other.combinedId
    }

    override fun hashCode(): Int {
        return combinedId.hashCode()
    }

    override fun toString(): String {
        return "IfSetPlayerHead(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId" +
            ")"
    }
}
