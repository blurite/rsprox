package net.rsprox.protocol.game.incoming.model.worldentities

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * OpWorldEntity messages are sent when a player clicks one of the five primary options on a worldentity.
 * It should be noted that this message will not handle 'OPWORLDENTITY6', as that message requires
 * different arguments.
 * @property index the index of the worldentity that was clicked
 * @property controlKey whether the control key was held down, used to invert movement speed
 * @property op the option clicked, ranging from 1 to 5(inclusive).
 */
@Suppress("MemberVisibilityCanBePrivate")
public class OpWorldEntity private constructor(
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

        other as OpWorldEntity

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
        "OpWorldEntity(" +
            "index=$index, " +
            "controlKey=$controlKey, " +
            "op=$op" +
            ")"
}
