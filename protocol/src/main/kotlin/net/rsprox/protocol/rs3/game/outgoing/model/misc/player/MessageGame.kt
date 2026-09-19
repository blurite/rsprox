package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class MessageGame(
    public val type: Int,
    public val channel: Int,
    public val flags: Int,
    public val sender: String?,
    public val alternateSender: String?,
    public val message: String,
) : IncomingServerGameMessage
