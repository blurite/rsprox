package net.rsprox.protocol.game.incoming.model.locs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * OpLoc messages are fired when a player clicks one of the five (excluding oploc6)
 * options on a loc in the game.
 * @property id the base(non-multi-transformed) id of the loc the player clicked on
 * @property x the absolute x coordinate of the south-western corner of the loc
 * @property z the absolute z coordinate of the south-western corner of the loc
 * @property controlKey whether the control key was held down, used to invert movement speed
 * @property op the option clicked, ranging from 1 to 5 (inclusive)
 * @property subop the sub option clicked, or 0 if no sub op.
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class OpLocV2 private constructor(
    private val _id: UShort,
    private val _x: UShort,
    private val _z: UShort,
    public val controlKey: Boolean,
    private val _op: UByte,
    private val _subop: UByte,
) : IncomingGameMessage {
    public constructor(
        id: Int,
        x: Int,
        z: Int,
        controlKey: Boolean,
        op: Int,
        subop: Int,
    ) : this(
        id.toUShort(),
        x.toUShort(),
        z.toUShort(),
        controlKey,
        op.toUByte(),
        subop.toUByte(),
    )

    public val id: Int
        get() = _id.toInt()
    public val x: Int
        get() = _x.toInt()
    public val z: Int
        get() = _z.toInt()
    public val op: Int
        get() = _op.toInt()
    public val subop: Int
        get() = _subop.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpLocV2

        if (_id != other._id) return false
        if (_x != other._x) return false
        if (_z != other._z) return false
        if (controlKey != other.controlKey) return false
        if (_op != other._op) return false
        if (_subop != other._subop) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _z.hashCode()
        result = 31 * result + controlKey.hashCode()
        result = 31 * result + _op.hashCode()
        result = 31 * result + _subop.hashCode()
        return result
    }

    override fun toString(): String =
        "OpLocV2(" +
            "id=$id, " +
            "x=$x, " +
            "z=$z, " +
            "controlKey=$controlKey, " +
            "op=$op, " +
            "subop=$subop" +
            ")"
}
