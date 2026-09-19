package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Replaces the local player's four movement-dependent sequence alternatives. */
public data class PlayerAnimSpecific(
    public val delay: Int,
    public val animation0: Int,
    public val animation1: Int,
    public val animation2: Int,
    public val animation3: Int,
) : IncomingServerGameMessage
