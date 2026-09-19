package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class ClearPlayerSnapshot(
    public val index: Int,
) : IncomingServerGameMessage
