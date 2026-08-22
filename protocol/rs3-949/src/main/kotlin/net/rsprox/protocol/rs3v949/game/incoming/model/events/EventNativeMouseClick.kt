package net.rsprox.protocol.rs3v949.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public class EventNativeMouseClick private constructor(
    private val _lastTransmittedMouseClick: UShort,
    private val _code: UByte,
    private val _x: UShort,
    private val _y: UShort,
) : IncomingGameMessage {
    public constructor(
        lastTransmittedMouseClick: Int,
        code: Int,
        x: Int,
        y: Int,
    ) : this(
        lastTransmittedMouseClick.toUShort(),
        code.toUByte(),
        x.toUShort(),
        y.toUShort(),
    )

    public val lastTransmittedMouseClick: Int
        get() = _lastTransmittedMouseClick.toInt()
    public val code: Int
        get() = _code.toInt()
    public val x: Int
        get() = _x.toInt()
    public val y: Int
        get() = _y.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventNativeMouseClick

        if (_lastTransmittedMouseClick != other._lastTransmittedMouseClick) return false
        if (_code != other._code) return false
        if (_x != other._x) return false
        if (_y != other._y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _lastTransmittedMouseClick.hashCode()
        result = 31 * result + _code.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _y.hashCode()
        return result
    }

    override fun toString(): String =
        "EventNativeMouseClick(" +
            "lastTransmittedMouseClick=$lastTransmittedMouseClick, " +
            "code=$code, " +
            "x=$x, " +
            "y=$y" +
            ")"
}
