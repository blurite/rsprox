package net.rsprox.protocol.rs3.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class SynthSound(
    public val id: Int,
    public val loops: Int,
    public val delay: Int,
    public val volume: Int,
    public val rate: Int,
) : IncomingServerGameMessage
