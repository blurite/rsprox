package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfMoveSub(
    public val destination: Long,
    public val source: Long,
) : IncomingServerGameMessage
