package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class SetPlayerOp(
    public val slot: Int,
    public val priority: Boolean,
    public val text: String,
    public val cursor: Int,
) : IncomingServerGameMessage
