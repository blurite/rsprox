package net.rsprox.protocol.rs3.game.incoming.model.locs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class OpLocT(
    public val controlKey: Int,
    public val x: Int,
    public val selectedSub: Int,
    public val z: Int,
    public val selectedObj: Int,
    public val id: Int,
    public val selectedCombinedId: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
