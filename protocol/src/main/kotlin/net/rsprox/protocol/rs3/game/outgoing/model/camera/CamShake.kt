package net.rsprox.protocol.rs3.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class CamShake(
    public val axis: Int,
    public val randomAmplitude: Int,
    public val sineAmplitude: Int,
    public val duration: Int,
    public val frequency: Int,
) : IncomingServerGameMessage
