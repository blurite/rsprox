package net.rsprox.protocol.rs3.game.outgoing.model.varbit

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class Varbit(
    public val id: Int,
    public val value: Int,
) : IncomingServerGameMessage
