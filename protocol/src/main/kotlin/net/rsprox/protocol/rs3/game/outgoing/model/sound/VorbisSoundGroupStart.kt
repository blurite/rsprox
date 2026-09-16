package net.rsprox.protocol.rs3.game.outgoing.model.sound

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class VorbisSoundGroupStart(
    public val group: Int,
) : IncomingServerGameMessage
