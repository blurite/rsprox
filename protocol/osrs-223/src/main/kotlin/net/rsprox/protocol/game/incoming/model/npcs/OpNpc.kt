package net.rsprox.protocol.game.incoming.model.npcs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * OpNpc messages are sent when a player clicks one of the five primary options on a NPC.
 * It should be noted that this message will not handle 'OPNPC6', as that message requires
 * different arguments.
 * @property index the index of the npc that was clicked
 * @property controlKey whether the control key was held down, used to invert movement speed
 * @property op the option clicked, ranging from 1 to 5(inclusive).
 */
@Suppress("MemberVisibilityCanBePrivate")
public class OpNpc private constructor(
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

        other as OpNpc

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

    override fun toString(): String {
        return "OpNpc(" +
            "index=$index, " +
            "controlKey=$controlKey, " +
            "op=$op" +
            ")"
    }
}
