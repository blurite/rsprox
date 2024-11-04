package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * If set-hide is used to hide or unhide a component and its children on an interface.
 * @property interfaceId the interface id on which the component to hide or unhide resides on
 * @property componentId the component on the [interfaceId] to hide or unhide
 * @property hidden whether to hide or unhide the component
 */
public class IfSetHide private constructor(
    public val combinedId: CombinedId,
    public val hidden: Boolean,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        hidden: Boolean,
    ) : this(
        CombinedId(interfaceId, componentId),
        hidden,
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetHide

        if (combinedId != other.combinedId) return false
        if (hidden != other.hidden) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + hidden.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetHide(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "hidden=$hidden" +
            ")"
    }
}
