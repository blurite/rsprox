package net.rsprox.protocol.rs3.game.incoming.model.chat

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprox.protocol.rs3.common.QuickChat

public data class MessageQuickchatPublic(
    public val phraseId: Int,
    public val quickChat: QuickChat,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
