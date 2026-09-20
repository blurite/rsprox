package net.rsprox.protocol.rs3.game.incoming.model.social

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class ClanJoinChatLeaveChat(
    /** Null means the empty-body leave action; otherwise this is the NUL-terminated channel name. */
    public val name: String?,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
