package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Set chat filter settings is sent when the player changes either their
 * public, private or trade filters, in order to synchronize the status
 * with the server.
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
 * @property publicChatFilter the public chat filter status, any value in the above table
 * @property privateChatFilter the private chat filter status, allowed values include
 * 'On', 'Friends' and 'Off' (see table above)
 * @property tradeChatFilter the trade chat filter status, allowed values include
 * 'On', 'Friends' and 'Off' (see table above)
 */
@Suppress("MemberVisibilityCanBePrivate")
public class SetChatFilterSettings private constructor(
    private val _publicChatFilter: UByte,
    private val _privateChatFilter: UByte,
    private val _tradeChatFilter: UByte,
) : IncomingGameMessage {
    public constructor(
        publicChatFilter: Int,
        privateChatFilter: Int,
        tradeChatFilter: Int,
    ) : this(
        publicChatFilter.toUByte(),
        privateChatFilter.toUByte(),
        tradeChatFilter.toUByte(),
    )

    public val publicChatFilter: Int
        get() = _publicChatFilter.toInt()
    public val privateChatFilter: Int
        get() = _privateChatFilter.toInt()
    public val tradeChatFilter: Int
        get() = _tradeChatFilter.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetChatFilterSettings

        if (_publicChatFilter != other._publicChatFilter) return false
        if (_privateChatFilter != other._privateChatFilter) return false
        if (_tradeChatFilter != other._tradeChatFilter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _publicChatFilter.hashCode()
        result = 31 * result + _privateChatFilter.hashCode()
        result = 31 * result + _tradeChatFilter.hashCode()
        return result
    }

    override fun toString(): String =
        "SetChatFilterSettings(" +
            "publicChatFilter=$publicChatFilter, " +
            "privateChatFilter=$privateChatFilter, " +
            "tradeChatFilter=$tradeChatFilter" +
            ")"
}
