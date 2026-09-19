package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetClickMask(
    public val enabled: Boolean,
    public val componentHash: Long,
) : IncomingServerGameMessage
