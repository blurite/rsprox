package net.rsprox.protocol.rs3.game.incoming.model.buttons

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class IfTextChange(
    public val combinedId: Int,
    public val sub: Int,
    public val text: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
