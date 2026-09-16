package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfOpenTop(
    public val interfaceId: Int,
    public val legacyWord0: Int? = null,
    public val legacyWord1: Int? = null,
    public val legacyWord2: Int? = null,
    public val unused: Int? = null,
    public val legacyWord3: Int? = null,
) : IncomingServerGameMessage
