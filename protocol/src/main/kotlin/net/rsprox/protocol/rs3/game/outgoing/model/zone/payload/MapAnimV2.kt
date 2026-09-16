package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class MapAnimV2(
    public val xInZone: Int,
    public val zInZone: Int,
    public val id: Int,
    public val height: Int,
    public val delay: Int,
    public val angle: Int,
    public val attachment: Int,
    public val unused0: Int? = null,
    public val unused1: Int? = null,
    public val unused2: Int? = null,
) : IncomingServerGameMessage
