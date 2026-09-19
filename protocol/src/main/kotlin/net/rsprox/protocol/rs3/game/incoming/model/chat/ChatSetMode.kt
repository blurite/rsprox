package net.rsprox.protocol.rs3.game.incoming.model.chat

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class ChatSetMode(
    public val channel: Int,
    public val mode: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
