package net.rsprox.protocol.rs3.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprox.protocol.rs3.common.TypedVariable

public data class StoreServerPermVarcs(
    public val complete: Int,
    public val variables: List<TypedVariable>,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT
}
