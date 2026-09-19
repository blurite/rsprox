package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfOpenSub(
    public val componentHash: Long,
    public val childId: Int,
    public val layer: Int,
    public val legacyWord0: Int? = null,
    public val legacyWord1: Int? = null,
    public val legacyWord2: Int? = null,
    public val legacyWord3: Int? = null,
) : IncomingServerGameMessage
