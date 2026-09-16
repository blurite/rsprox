package net.rsprox.protocol.rs3.game.incoming.model.players

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class OpPlayerT(
    public val controlKey: Int,
    public val index: Int,
    public val selectedSub: Int,
    public val selectedObj: Int,
    public val selectedCombinedId: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
