package net.rsprox.protocol.rs3.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class Cam2Enable(
    public val enabled: Boolean,
) : IncomingServerGameMessage
