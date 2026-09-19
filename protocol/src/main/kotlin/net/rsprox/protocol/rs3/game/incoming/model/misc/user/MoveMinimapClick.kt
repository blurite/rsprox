package net.rsprox.protocol.rs3.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class MoveMinimapClick(
    public val z: Int,
    public val controlKey: Int,
    public val x: Int,
    public val reservedMinimapMetadata: List<Int>,
    public val playerX: Int,
    public val playerZ: Int,
    public val sentinel: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
