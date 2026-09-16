package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class SendPing(
    public val echo0: Int,
    public val echo1: Int,
) : IncomingServerGameMessage
