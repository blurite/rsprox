package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetRecol(
    public val index: Int,
    public val componentHash: Long,
    public val destination: Int,
    public val source: Int,
) : IncomingServerGameMessage
