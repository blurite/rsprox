package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class MessagePrivate(
    public val alternateSenderFlag: Int,
    public val sender: String,
    public val alternateSender: String?,
    public val messageWorld: Int,
    public val messageCounter: Int,
    public val playerType: Int,
    public val message: String,
) : IncomingServerGameMessage
