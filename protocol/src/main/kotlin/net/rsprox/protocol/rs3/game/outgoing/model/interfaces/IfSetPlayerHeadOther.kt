package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetPlayerHeadOther(
    public val appearanceHash: Int,
    public val componentHash: Long,
    public val playerIndex: Int,
) : IncomingServerGameMessage
