package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetObjectLongV2(
    public val quantity: Long,
    public val componentHash: Long,
    public val objId: Int,
) : IncomingServerGameMessage
