package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * If clear-inv messaged are used to clear all objs on any if-1 type
 * component. As there are very few if-1 type old interfaces remaining,
 * this packet is mostly unused nowadays.
 * @property interfaceId the id of the interface on which the inv exists
 * @property componentId the id of the component on the [interfaceId] to be cleared
 */
public class IfClearInv private constructor(
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

        other as IfClearInv

        return combinedId == other.combinedId
    }

    override fun hashCode(): Int {
        return combinedId.hashCode()
    }

    override fun toString(): String {
        return "IfClearInv(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId" +
            ")"
    }
}
