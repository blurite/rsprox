package net.rsprox.protocol.rs3v949.game.incoming.model.buttons

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public class If3Button private constructor(
    private val _combinedId: CombinedId,
    private val _obj: Int,
    private val _slot: UShort,
    public val op: Int,
) : IncomingGameMessage {
    public constructor(
        combinedId: CombinedId,
        obj: Int,
        slot: Int,
        op: Int,
    ) : this(
        combinedId,
        obj,
        slot.toUShort(),
        op,
    )

    public val combinedId: Int
        get() = _combinedId.combinedId
    public val obj: Int
        get() = if (_obj == MEDIUM_SENTINEL) -1 else _obj
    public val slot: Int
        get() = if (_slot.toInt() == USHORT_SENTINEL) -1 else _slot.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as If3Button

        if (_combinedId != other._combinedId) return false
        if (_obj != other._obj) return false
        if (_slot != other._slot) return false
        if (op != other.op) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _combinedId.hashCode()
        result = 31 * result + _obj
        result = 31 * result + _slot.hashCode()
        result = 31 * result + op
        return result
    }

    override fun toString(): String =
        "If3Button(" +
            "combinedId=$combinedId, " +
            "obj=$obj, " +
            "slot=$slot, " +
            "op=$op" +
            ")"

    private companion object {
        private const val MEDIUM_SENTINEL = 0xFFFFFF
        private const val USHORT_SENTINEL = 0xFFFF
    }
}
