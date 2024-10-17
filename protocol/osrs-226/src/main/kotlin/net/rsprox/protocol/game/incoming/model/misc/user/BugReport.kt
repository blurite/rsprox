package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Bug report packets are sent when players submit a bug report
 * using the bug report interface.
 * @property type the type of the report. The only known value of this is 0.
 * @property description the description of the bug, how it happened etc.
 * The maximum length of this form is 500 characters, as the client prevents
 * sending anything beyond that.
 * @property instructions instructions on how to reproduce the bug.
 * The maximum length of this form is also 500 characters, as the client
 * prevents sending anything beyond that.
 * The decoder will throw an exception if the length of the message exceeds
 * the 500 length constraint, so no validation needs to be done on the user's end.
 */
public class BugReport private constructor(
    private val _type: UByte,
    public val description: String,
    public val instructions: String,
) : IncomingGameMessage {
    public constructor(
        type: Int,
        description: String,
        instructions: String,
    ) : this(
        type.toUByte(),
        description,
        instructions,
    )

    public val type: Int
        get() = _type.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BugReport

        if (_type != other._type) return false
        if (description != other.description) return false
        if (instructions != other.instructions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _type.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + instructions.hashCode()
        return result
    }

    override fun toString(): String =
        "BugReport(" +
            "description='$description', " +
            "instructions='$instructions', " +
            "type=$type" +
            ")"
}
