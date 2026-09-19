package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetRetex(
    public val destination: Int,
    public val index: Int,
    public val source: Int,
    public val componentHash: Long,
) : IncomingServerGameMessage
