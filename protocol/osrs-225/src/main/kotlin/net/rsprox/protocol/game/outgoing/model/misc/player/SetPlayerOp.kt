package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Set player op packet is used to set the right-click
 * option on all players to a specific option.
 * @property id the id of the option to change, a value in range of
 * 1 to 8 (inclusive)
 * @property priority whether the option should get priority
 * over the 'Walk here' option.
 * @property op the option string to set, or null if removing an op.
 */
public class SetPlayerOp private constructor(
    private val _id: UByte,
    public val priority: Boolean,
    public val op: String?,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        priority: Boolean,
        op: String?,
    ) : this(
        id.toUByte(),
        priority,
        op,
    )

    public val id: Int
        get() = _id.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetPlayerOp

        if (_id != other._id) return false
        if (priority != other.priority) return false
        if (op != other.op) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + priority.hashCode()
        result = 31 * result + op.hashCode()
        return result
    }

    override fun toString(): String {
        return "SetPlayerOp(" +
            "id=$id, " +
            "priority=$priority, " +
            "op='$op'" +
            ")"
    }
}
