package net.rsprox.protocol.rs3.game.incoming.model.account

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class CreateLogProgress(
    public val progress: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT
}
