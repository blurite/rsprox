package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class DoCheat(
    public val command: String,
) : IncomingServerGameMessage
