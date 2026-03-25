package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * If set model packet is used to set a model to render on an interface.
 * The component must be of model type for this to succeed.
 * @property interfaceId the interface id on which to set the events
 * @property componentId the component on that interface to set the events on
 * @property model the id of the model to render.
 */
public class IfSetModelV2(
    public val combinedId: CombinedId,
    public val model: Int,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        model: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        model,
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetModelV2

        if (combinedId != other.combinedId) return false
        if (model != other.model) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + model.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetModelV2(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "model=$model" +
            ")"
    }
}
