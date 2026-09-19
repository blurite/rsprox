package net.rsprox.protocol.rs3.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class ClientCheat(
    public val flagA: Int,
    public val flagB: Int,
    public val command: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
