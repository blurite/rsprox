package net.rsprox.protocol.game.incoming.model.objs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * OpObj messages are fired when the player interacts with an obj on the ground,
 * e.g. picking an obj up off the ground. This does not include examining objs.
 * @property id the id of the obj interacted with
 * @property x the absolute x coordinate of the obj on the ground
 * @property z the absolute z coordinate of the obj on the ground
 * @property controlKey whether the control key was held down, used to invert movement speed
 * @property op the option clicked, ranging from 1 to 5 (inclusive)
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class OpObj private constructor(
    private val _id: UShort,
    private val _x: UShort,
    private val _z: UShort,
    public val controlKey: Boolean,
    private val _op: UByte,
) : IncomingGameMessage {
    public constructor(
        id: Int,
        x: Int,
        z: Int,
        controlKey: Boolean,
        op: Int,
    ) : this(
        id.toUShort(),
        x.toUShort(),
        z.toUShort(),
        controlKey,
        op.toUByte(),
    )

    public val id: Int
        get() = _id.toInt()
    public val x: Int
        get() = _x.toInt()
    public val z: Int
        get() = _z.toInt()
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
        if (_z != other._z) return false
        if (controlKey != other.controlKey) return false
        if (_op != other._op) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _z.hashCode()
        result = 31 * result + controlKey.hashCode()
        result = 31 * result + _op.hashCode()
        return result
    }

    override fun toString(): String {
        return "OpObj(" +
            "id=$id, " +
            "x=$x, " +
            "z=$z, " +
            "controlKey=$controlKey, " +
            "op=$op" +
            ")"
    }
}
