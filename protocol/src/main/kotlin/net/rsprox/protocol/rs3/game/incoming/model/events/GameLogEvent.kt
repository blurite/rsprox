package net.rsprox.protocol.rs3.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprox.protocol.rs3.common.TypedVariable

public data class GameLogEvent(
    public val id: Int,
    public val value16: Int,
    public val script: Int,
    public val arguments: List<Argument>,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    public data class Argument(
        public val scriptType: Int,
        public val value: TypedVariable.Value,
    )
}
