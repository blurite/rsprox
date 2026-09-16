package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class ChangeLobby(
    public val host: String,
    public val world: Int,
    public val port: Int,
    public val alternatePort: Int,
) : IncomingServerGameMessage
