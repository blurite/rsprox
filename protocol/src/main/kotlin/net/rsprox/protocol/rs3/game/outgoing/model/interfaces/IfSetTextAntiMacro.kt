package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetTextAntiMacro(
    public val componentHash: Long,
    public val enabled: Boolean,
) : IncomingServerGameMessage
