package net.rsprox.protocol.rs3.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/** Lua-plugin report; field meanings remain under investigation. */
public data class UnnamedBatch80(
    public val complete: Boolean,
    public val entries: List<Entry>,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    public data class Entry(
        public val field0: Int,
        public val field1: Int,
        public val field2: Int,
        public val field3: Int,
    )
}
