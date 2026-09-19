package net.rsprox.protocol.rs3.game.outgoing.model.group

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.common.TypedVariable

public data class PlayerGroupVarps(
    public val memberIndex: Int,
    public val clear: Int,
    public val variables: List<TypedVariable>,
) : IncomingServerGameMessage
