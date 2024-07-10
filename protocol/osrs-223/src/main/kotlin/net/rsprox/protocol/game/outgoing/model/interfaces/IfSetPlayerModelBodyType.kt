package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * If setplayermodel bodytype is used to change the current body-type of
 * a player model on an interface, making the client prefer swap out
 * the models for the respective type.
 * @property interfaceId the id of the interface on which the model resides
 * @property componentId the id of the component on which the model resides
 * @property bodyType the new body-type to set to the player model
 */
public class IfSetPlayerModelBodyType private constructor(
    public val combinedId: CombinedId,
    private val _bodyType: UByte,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        bodyType: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        bodyType.toUByte(),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val bodyType: Int
        get() = _bodyType.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetPlayerModelBodyType

        if (combinedId != other.combinedId) return false
        if (_bodyType != other._bodyType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _bodyType.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetPlayerModelBodyType(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "bodyType=$bodyType" +
            ")"
    }
}
