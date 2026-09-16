package net.rsprox.protocol.rs3.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class CamForceAngle(
    public val angle0: Int,
    public val angle1: Int,
) : IncomingServerGameMessage
