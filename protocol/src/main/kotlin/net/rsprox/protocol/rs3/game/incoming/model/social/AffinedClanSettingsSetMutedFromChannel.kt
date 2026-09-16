package net.rsprox.protocol.rs3.game.incoming.model.social

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class AffinedClanSettingsSetMutedFromChannel(
    public val channel: Int,
    public val member: Int,
    public val muted: Int,
    public val name: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
