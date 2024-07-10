package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * If set-position events are used to move a component on an interface.
 * @property interfaceId the interface on which the component to move exists
 * @property componentId the component id to move
 * @property x the x coordinate to move to
 * @property y the y coordinate to move to
 */
public class IfSetPosition private constructor(
    public val combinedId: CombinedId,
    private val _x: UShort,
    private val _y: UShort,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        x: Int,
        y: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        x.toUShort(),
        y.toUShort(),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val x: Int
        get() = _x.toInt()
    public val y: Int
        get() = _y.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetPosition

        if (combinedId != other.combinedId) return false
        if (_x != other._x) return false
        if (_y != other._y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _y.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetPosition(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "x=$x, " +
            "y=$y" +
            ")"
    }
}
