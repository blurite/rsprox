package net.rsprox.protocol.rs3.game.incoming.model.objs

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class OpObjT(
    public val x: Int,
    public val flags: Int,
    public val selectedSub: Int,
    public val selectedObj: Int,
    public val z: Int,
    public val selectedCombinedId: Int,
    public val id: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
