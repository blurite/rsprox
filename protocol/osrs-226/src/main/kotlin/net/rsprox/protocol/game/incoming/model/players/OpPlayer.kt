package net.rsprox.protocol.game.incoming.model.players

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Opplayer messages are fired whenever a player clicks an option on another player,
 * or if messages such as "* wishes to trade with you." are clicked.
 * In the case of latter, only ops 1, 4, 6 and 7 will fire the packet.
 * @property index the index of the player who was interacted with
 * @property controlKey whether the control key was held down, used to invert movement speed
 * @property op the option clicked, ranging from 1 to 8 (inclusive)
 */
@Suppress("MemberVisibilityCanBePrivate")
public class OpPlayer private constructor(
    private val _index: UShort,
    public val controlKey: Boolean,
    private val _op: UByte,
) : IncomingGameMessage {
    public constructor(
        index: Int,
        controlKey: Boolean,
        op: Int,
    ) : this(
        index.toUShort(),
        controlKey,
        op.toUByte(),
    )

    public val index: Int
        get() = _index.toInt()
    public val op: Int
        get() = _op.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpPlayer

        if (_index != other._index) return false
        if (controlKey != other.controlKey) return false
        if (_op != other._op) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _index.hashCode()
        result = 31 * result + controlKey.hashCode()
        result = 31 * result + _op.hashCode()
        return result
    }

    override fun toString(): String =
        "OpPlayer(" +
            "index=$index, " +
            "controlKey=$controlKey, " +
            "op=$op" +
            ")"
}
