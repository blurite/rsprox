package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetPlayerModelOther(
    public val appearanceHash: Int,
    public val playerIndex: Int,
    public val componentHash: Long,
) : IncomingServerGameMessage
