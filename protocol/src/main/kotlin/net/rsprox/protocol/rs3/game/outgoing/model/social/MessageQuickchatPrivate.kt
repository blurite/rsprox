package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.common.QuickChat

public data class MessageQuickchatPrivate(
    public val alternateSenderFlag: Int,
    public val sender: String,
    public val alternateSender: String?,
    public val messageWorld: Int,
    public val messageCounter: Int,
    public val playerType: Int,
    public val phraseId: Int,
    public val quickChat: QuickChat,
) : IncomingServerGameMessage
