package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Sets an object on an interface component.
 * @property interfaceId the interface on which the component resides
 * @property componentId the component on which the obj resides
 * @property obj the id of the obj to set on the component
 * @property count the count of the obj, used to obtain a different variant
 * of the model of the obj
 */
public class IfSetObject private constructor(
    public val combinedId: CombinedId,
    private val _obj: UShort,
    private val _count: UShort,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        obj: Int,
        count: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        obj.toUShort(),
        count.toUShort(),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val obj: Int
        get() = _obj.toInt()
    public val count: Int
        get() = _count.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetObject

        if (combinedId != other.combinedId) return false
        if (_obj != other._obj) return false
        if (_count != other._count) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _obj.hashCode()
        result = 31 * result + _count.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetObject(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "obj=$obj, " +
            "count=$count" +
            ")"
    }
}
