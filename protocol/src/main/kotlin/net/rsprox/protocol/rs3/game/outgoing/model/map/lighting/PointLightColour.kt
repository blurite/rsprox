package net.rsprox.protocol.rs3.game.outgoing.model.map.lighting

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class PointLightColour(
    public val colour: Int,
    public val duration: Int,
    public val id: Int,
) : IncomingServerGameMessage
