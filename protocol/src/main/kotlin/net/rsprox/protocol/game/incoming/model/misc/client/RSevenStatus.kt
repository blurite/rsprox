package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * RSeven Status packet is sent to inform the client of various RT7 related properties on login.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class RSevenStatus private constructor(
    private val _packedValue: UByte,
) : IncomingGameMessage {
    public constructor(packedValue: Int) : this(packedValue.toUByte())

    public val packedValue: Int
        get() = _packedValue.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RSevenStatus

        return _packedValue == other._packedValue
    }

    override fun hashCode(): Int {
        return _packedValue.hashCode()
    }

    override fun toString(): String {
        return "RSevenStatus(packedValue=$packedValue)"
    }
}
