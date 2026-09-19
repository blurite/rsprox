package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.common.QuickChat

public data class MessageQuickchatPrivateEcho(
    public val recipient: String,
    public val phraseId: Int,
    public val quickChat: QuickChat,
) : IncomingServerGameMessage
