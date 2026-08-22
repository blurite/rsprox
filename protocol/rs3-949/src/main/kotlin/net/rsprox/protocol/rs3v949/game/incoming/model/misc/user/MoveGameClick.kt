package net.rsprox.protocol.rs3v949.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public class MoveGameClick private constructor(
    private val _x: UShort,
    private val _y: UShort,
    public val run: Boolean,
) : IncomingGameMessage {
    public constructor(
        x: Int,
        y: Int,
        run: Boolean,
    ) : this(
        x.toUShort(),
        y.toUShort(),
        run,
    )

    public val x: Int
        get() = _x.toInt()
    public val y: Int
        get() = _y.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MoveGameClick

        if (_x != other._x) return false
        if (_y != other._y) return false
        if (run != other.run) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _x.hashCode()
        result = 31 * result + _y.hashCode()
        result = 31 * result + run.hashCode()
        return result
    }

    override fun toString(): String = "MoveGameClick(x=$x, y=$y, run=$run)"
}
