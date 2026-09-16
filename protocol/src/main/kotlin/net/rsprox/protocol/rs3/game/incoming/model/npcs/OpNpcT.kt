package net.rsprox.protocol.rs3.game.incoming.model.npcs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class OpNpcT(
    public val index: Int,
    public val selectedObj: Int,
    public val controlKey: Int,
    public val selectedSub: Int,
    public val selectedCombinedId: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
