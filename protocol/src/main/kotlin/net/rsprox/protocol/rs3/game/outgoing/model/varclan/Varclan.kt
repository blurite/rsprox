package net.rsprox.protocol.rs3.game.outgoing.model.varclan

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.common.TypedVariable

public data class Varclan(
    public val id: Int,
    public val variable: TypedVariable,
) : IncomingServerGameMessage
