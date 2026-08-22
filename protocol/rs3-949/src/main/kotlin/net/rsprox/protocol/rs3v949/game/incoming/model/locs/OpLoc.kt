package net.rsprox.protocol.rs3v949.game.incoming.model.locs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public class OpLoc private constructor(
    private val _id: UInt,
    private val _x: UShort,
    private val _y: UShort,
    private val _op: UByte,
    public val run: Boolean,
) : IncomingGameMessage {
    public constructor(
        id: Int,
        x: Int,
        y: Int,
        op: Int,
        run: Boolean,
    ) : this(
        id.toUInt(),
        x.toUShort(),
        y.toUShort(),
        op.toUByte(),
        run,
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

        other as OpLoc

        if (_id != other._id) return false
        if (_x != other._x) return false
        if (_y != other._y) return false
        if (_op != other._op) return false
        if (run != other.run) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _y.hashCode()
        result = 31 * result + _op.hashCode()
        result = 31 * result + run.hashCode()
        return result
    }

    override fun toString(): String =
        "OpLoc(id=$id, x=$x, y=$y, op=$op, run=$run)"
}
