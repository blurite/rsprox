package net.rsprox.protocol.rs3.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class SoundMixbussSetLevel(
    public val bus: Int,
    public val level: Int,
) : IncomingServerGameMessage
