package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetAngle(
    public val zoom: Int,
    public val angle1: Int,
    public val angle0: Int,
    public val componentHash: Long,
) : IncomingServerGameMessage
