package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class MessagePrivateEcho(
    public val recipient: String,
    public val message: String,
) : IncomingServerGameMessage
