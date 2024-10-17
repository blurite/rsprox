package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * If setplayermodel self is used to set the player model on an interface
 * to that of the local player.
 * @property interfaceId the id of the interface on which the model resides
 * @property componentId the id of the component on which the model resides
 * @property copyObjs whether to copy all the worn objs over as well
 */
public class IfSetPlayerModelSelf private constructor(
    public val combinedId: CombinedId,
    public val copyObjs: Boolean,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        copyObjs: Boolean,
    ) : this(
        CombinedId(interfaceId, componentId),
        copyObjs,
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetPlayerModelSelf

        if (combinedId != other.combinedId) return false
        if (copyObjs != other.copyObjs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + copyObjs.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetPlayerModelSelf(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "copyObjs=$copyObjs" +
            ")"
    }
}
