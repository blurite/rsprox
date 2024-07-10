package net.rsprox.protocol.game.incoming.model.objs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * OpObj6 messages are fired whenever a player examines an obj on the ground.
 * @property id the id of the obj examined
 * @property x the absolute x coordinate of the obj on the ground
 * @property z the absolute z coordinate of the obj on the ground
 */
public class OpObj6 private constructor(
    private val _id: UShort,
    private val _x: UShort,
    private val _z: UShort,
) : IncomingGameMessage {
    public constructor(
        id: Int,
        x: Int,
        z: Int,
    ) : this(
        id.toUShort(),
        x.toUShort(),
        z.toUShort(),
    )

    public val id: Int
        get() = _id.toInt()
    public val x: Int
        get() = _x.toInt()
    public val z: Int
        get() = _z.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpObj6

        if (_id != other._id) return false
        if (_x != other._x) return false
        if (_z != other._z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _z.hashCode()
        return result
    }

    override fun toString(): String {
        return "OpObj6(" +
            "id=$id, " +
            "x=$x, " +
            "z=$z" +
            ")"
    }
}
