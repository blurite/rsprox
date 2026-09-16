package net.rsprox.protocol.rs3.game.outgoing.model.account

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class UpdateDob(
    public val dateOfBirth: Int,
    public val verified: Boolean,
) : IncomingServerGameMessage
