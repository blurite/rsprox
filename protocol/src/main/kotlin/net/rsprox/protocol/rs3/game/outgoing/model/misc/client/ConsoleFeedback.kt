package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class ConsoleFeedback(
    public val unusedText: String,
    public val prefix: String,
    public val totalMatches: Int,
    public val resultCount: Int,
    public val results: List<String>,
) : IncomingServerGameMessage
