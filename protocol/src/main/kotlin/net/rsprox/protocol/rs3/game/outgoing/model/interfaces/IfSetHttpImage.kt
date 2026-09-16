package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetHttpImage(
    public val componentHash: Long,
    public val imageId: Int,
) : IncomingServerGameMessage
