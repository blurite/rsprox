package net.rsprox.protocol.rs3.game.outgoing.model.account

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class CreateCheckEmailReply(
    public val response: Int,
) : IncomingServerGameMessage
