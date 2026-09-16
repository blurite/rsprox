package net.rsprox.protocol.rs3.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class BugReport(
    public val reportCategory: Int,
    public val details: String,
    public val summary: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
