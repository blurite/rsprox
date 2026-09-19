package net.rsprox.protocol.rs3.game.outgoing.model.varc

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class VarcLong(
    public val id: Int,
    public val value: Long,
) : IncomingServerGameMessage
