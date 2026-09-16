package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class MapAnim(
    public val id: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val height: Int,
    public val delay: Int,
    public val rotation: Int,
    public val unused0: Int? = null,
    public val unused1: Int? = null,
    public val unused2: Int? = null,
) : IncomingServerGameMessage
