package net.rsprox.protocol.rs3.game.incoming.model.npcs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public class OpNpc private constructor(
    private val _index: UShort,
    private val _op: UByte,
    public val run: Boolean,
) : IncomingGameMessage {
    public constructor(
        index: Int,
        op: Int,
        run: Boolean,
    ) : this(
        index.toUShort(),
        op.toUByte(),
        run,
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
        if (_op != other._op) return false
        if (run != other.run) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _index.hashCode()
        result = 31 * result + _op.hashCode()
        result = 31 * result + run.hashCode()
        return result
    }

    override fun toString(): String =
        "OpNpc(index=$index, op=$op, run=$run)"
}
