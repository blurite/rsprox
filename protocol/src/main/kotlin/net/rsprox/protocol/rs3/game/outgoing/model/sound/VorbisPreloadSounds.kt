package net.rsprox.protocol.rs3.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class VorbisPreloadSounds(
    public val id: Int,
) : IncomingServerGameMessage
