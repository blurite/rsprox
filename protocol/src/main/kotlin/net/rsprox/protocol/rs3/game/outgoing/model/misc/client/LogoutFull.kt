package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class LogoutFull(
    public val reason: Int,
) : IncomingServerGameMessage
