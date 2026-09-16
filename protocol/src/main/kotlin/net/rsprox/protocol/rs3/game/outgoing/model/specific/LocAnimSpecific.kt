package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class LocAnimSpecific(
    public val shapeRotation: Int,
    public val speed: Int,
    public val animation: Int,
    public val coordinate: Int,
) : IncomingServerGameMessage
