package net.rsprox.protocol.rs3.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

public data class EventNativeMouseMove(
    public val meanRemainder: Int,
    public val remainder: Int,
    public val movements: List<MouseMovement>,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT
}
