package net.rsprox.protocol.rs3.game.outgoing.model.selection

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class SetLocOpOverride(
    public val overrideStart: Int,
    public val operation: Int,
    public val label: String,
    public val cursor: Int,
    public val overrideEnd: Int,
) : IncomingServerGameMessage
