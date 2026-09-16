package net.rsprox.protocol.rs3.game.outgoing.model.varc

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class VarcBit(
    public val id: Int,
    public val value: Int,
) : IncomingServerGameMessage
