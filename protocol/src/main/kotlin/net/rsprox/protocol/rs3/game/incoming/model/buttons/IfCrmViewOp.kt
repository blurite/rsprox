package net.rsprox.protocol.rs3.game.incoming.model.buttons

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class IfCrmViewOp(
    public val sub: Int,
    public val crmValue0: Int,
    public val crmValue2: Int,
    public val crmValue1: Int,
    public val combinedId: Int,
    public val selectedCrmEntry: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
