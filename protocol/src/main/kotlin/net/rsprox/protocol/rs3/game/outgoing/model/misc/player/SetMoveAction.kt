package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** A null action selects the client's localized default; an empty string is an explicit label. */
public data class SetMoveAction(
    public val action: String?,
    public val cursor: Int,
) : IncomingServerGameMessage
