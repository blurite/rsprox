package net.rsprox.protocol.game.incoming.model.resumed

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * Resume pausebutton messages are sent when the player continues
 * a dialogue through the "Click to continue" button
 * @property interfaceId the interface on which the component exists
 * @property componentId the component id clicked
 * @property sub the subcomponent id, or -1 if it doesn't exist
 */
public class ResumePauseButton private constructor(
    private val combinedId: CombinedId,
    private val _sub: UShort,
) : IncomingGameMessage {
    public constructor(
        combinedId: CombinedId,
        sub: Int,
    ) : this(
        combinedId,
        sub.toUShort(),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val sub: Int
        get() = _sub.toIntOrMinusOne()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResumePauseButton

        if (combinedId != other.combinedId) return false
        if (_sub != other._sub) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _sub.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResumePauseButton(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "sub=$sub" +
            ")"
    }
}
