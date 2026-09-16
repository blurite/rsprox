package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfSetPlayerHeadIgnoreWorn(
    public val kitLow: Int,
    public val kitExtra: Int,
    public val kitHigh: Int,
    public val componentHash: Long,
) : IncomingServerGameMessage
