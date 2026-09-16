package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class UrlOpen(
    public val mode: Int,
    public val url: String,
    public val preferredUrl: String?,
) : IncomingServerGameMessage
