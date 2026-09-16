package net.rsprox.protocol.rs3.game.outgoing.model.selection

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Both packed coordinates, the ID and count are transmitted even when selection is disabled. */
public data class LocSelectConfigure(
    public val id: Int,
    public val enabled: Boolean,
    public val from: Int,
    public val count: Int,
    public val to: Int,
) : IncomingServerGameMessage
