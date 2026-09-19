package net.rsprox.protocol.rs3.game.incoming.model.objs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public class OpObj private constructor(
    private val _id: UInt,
    private val _x: UShort,
    private val _y: UShort,
    private val _op: UByte,
    public val run: Boolean,
    /** Full revision-specific flags when decoded; null for older decoders that only supplied run. */
    public val flags: Int?,
) : IncomingGameMessage {
    public constructor(
        id: Int,
        x: Int,
        y: Int,
        op: Int,
        run: Boolean,
        flags: Int? = null,
    ) : this(
        id.toUInt(),
        x.toUShort(),
        y.toUShort(),
        op.toUByte(),
        run,
        flags,
    )

    public val id: Int
        get() = _id.toInt()
    public val x: Int
        get() = _x.toInt()
    public val y: Int
        get() = _y.toInt()
    public val op: Int
        get() = _op.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpObj

        if (_id != other._id) return false
        if (_x != other._x) return false
        if (_y != other._y) return false
        if (_op != other._op) return false
        if (run != other.run) return false
        if (flags != other.flags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _y.hashCode()
        result = 31 * result + _op.hashCode()
        result = 31 * result + run.hashCode()
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "OpObj(id=$id, x=$x, y=$y, op=$op, run=$run, flags=$flags)"
}
