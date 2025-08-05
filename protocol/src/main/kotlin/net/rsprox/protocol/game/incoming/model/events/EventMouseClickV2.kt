package net.rsprox.protocol.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Mouse click messages are sent whenever the user clicks with the
 * right or left mouse button, and if the "Middle mouse button controls camera"
 * is disabled, middle buttons (the scroll wheel itself).
 * @property lastTransmittedMouseClick how many milliseconds since the last mouse
 * click event was transmitted
 * @property code the hook code from windows. This value is always 0 when used in Java.
 * See [link here](https://learn.microsoft.com/en-us/windows/win32/winmsg/about-hooks?redirectedfrom=MSDN).
 * @property rightClick whether a right mouse click was performed, or left/middle.
 * There is no distinction between left and middle transmitted to the server.
 * @property x the x coordinate clicked, always a positive integer, capped to the
 * client frame width.
 * @property y the y coordinate clicked, always a positive integer, capped to the
 * client frame height.
 */
public class EventMouseClickV2 private constructor(
    private val _lastTransmittedMouseClick: UShort,
    private val _code: UByte,
    public val rightClick: Boolean,
    private val _x: UShort,
    private val _y: UShort,
) : IncomingGameMessage {
    public constructor(
        lastTransmittedMouseClick: Int,
        code: Int,
        rightClick: Boolean,
        x: Int,
        y: Int,
    ) : this(
        lastTransmittedMouseClick.toUShort(),
        code.toUByte(),
        rightClick,
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

        other as EventMouseClickV2

        if (_lastTransmittedMouseClick != other._lastTransmittedMouseClick) return false
        if (_code != other._code) return false
        if (rightClick != other.rightClick) return false
        if (_x != other._x) return false
        if (_y != other._y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _lastTransmittedMouseClick.hashCode()
        result = 31 * result + _code.hashCode()
        result = 31 * result + rightClick.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _y.hashCode()
        return result
    }

    override fun toString(): String =
        "EventNativeMouseClickV2(" +
            "lastTransmittedMouseClick=$lastTransmittedMouseClick, " +
            "code=$code, " +
            "rightClick=$rightClick" +
            "x=$x, " +
            "y=$y" +
            ")"
}
