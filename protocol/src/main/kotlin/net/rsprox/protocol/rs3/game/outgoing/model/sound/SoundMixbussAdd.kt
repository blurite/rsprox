package net.rsprox.protocol.rs3.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class SoundMixbussAdd(
    public val bus: Int,
    public val parent: Int,
    public val level: Int,
) : IncomingServerGameMessage
