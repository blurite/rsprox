package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.common.QuickChat

public data class MessagePublic(
    public val playerIndex: Int,
    public val colourEffectAndQuickFlag: Int,
    public val playerType: Int,
    public val message: String?,
    public val phraseId: Int? = null,
    public val quickChat: QuickChat? = null,
) : IncomingServerGameMessage
