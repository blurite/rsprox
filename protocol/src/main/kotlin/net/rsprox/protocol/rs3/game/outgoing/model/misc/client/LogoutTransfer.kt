package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class LogoutTransfer(
    public val world: Int,
    public val host: String,
    public val port: Int,
    public val alternatePort: Int,
    public val transferFlag: Int,
) : IncomingServerGameMessage
