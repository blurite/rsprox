package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Window status is sent first on login, and afterwards whenever
 * the client changes window status.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class WindowStatus private constructor(
    private val _windowMode: UByte,
    private val _frameWidth: UShort,
    private val _frameHeight: UShort,
) : IncomingGameMessage {
    public constructor(
        windowMode: Int,
        frameWidth: Int,
        frameHeight: Int,
    ) : this(
        windowMode.toUByte(),
        frameWidth.toUShort(),
        frameHeight.toUShort(),
    )

    public val windowMode: Int
        get() = _windowMode.toInt()
    public val frameWidth: Int
        get() = _frameWidth.toInt()
    public val frameHeight: Int
        get() = _frameHeight.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WindowStatus

        if (_windowMode != other._windowMode) return false
        if (_frameWidth != other._frameWidth) return false
        if (_frameHeight != other._frameHeight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _windowMode.hashCode()
        result = 31 * result + _frameWidth.hashCode()
        result = 31 * result + _frameHeight.hashCode()
        return result
    }

    override fun toString(): String {
        return "WindowStatus(" +
            "windowMode=$windowMode, " +
            "frameWidth=$frameWidth, " +
            "frameHeight=$frameHeight" +
            ")"
    }
}
