package net.rsprox.protocol.rs3.game.outgoing.model.map.lighting

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class PointLightShadow(
    public val id: Int,
    public val encodedControl: Int,
    public val mode: Int,
) : IncomingServerGameMessage
