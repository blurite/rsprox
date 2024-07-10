package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Chat filter settings packed is used to set the public and
 * trade chat filters to the specified values.
 *
 * Chat filters table:
 * ```
 * | Id |   Type   |
 * |----|:--------:|
 * | 0  |    On    |
 * | 1  |  Friends |
 * | 2  |    Off   |
 * | 3  |   Hide   |
 * | 4  | Autochat |
 * ```
 *
 * @property publicChatFilter the public chat filter value, allowed values
 * include everything in the table above.
 * @property tradeChatFilter the trade chat filter value, allowed values include
 * 'On', 'Friends' and 'Off' (see table above)
 */
public class ChatFilterSettings private constructor(
    private val _publicChatFilter: UByte,
    private val _tradeChatFilter: UByte,
) : OutgoingGameMessage {
    public constructor(
        publicChatFilter: Int,
        tradeChatFilter: Int,
    ) : this(
        publicChatFilter.toUByte(),
        tradeChatFilter.toUByte(),
    )

    public val publicChatFilter: Int
        get() = _publicChatFilter.toInt()
    public val tradeChatFilter: Int
        get() = _tradeChatFilter.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatFilterSettings

        if (_publicChatFilter != other._publicChatFilter) return false
        if (_tradeChatFilter != other._tradeChatFilter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _publicChatFilter.hashCode()
        result = 31 * result + _tradeChatFilter.hashCode()
        return result
    }

    override fun toString(): String {
        return "ChatFilterSettings(" +
            "publicChatFilter=$publicChatFilter, " +
            "tradeChatFilter=$tradeChatFilter" +
            ")"
    }
}
