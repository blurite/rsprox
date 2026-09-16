package net.rsprox.protocol.rs3.game.incoming.model.buttons

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class IfButtonT(
    public val selectedSub: Int,
    public val selectedObj: Int,
    public val targetCombinedId: Int,
    public val targetSub: Int,
    public val selectedCombinedId: Int,
    public val targetObj: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
