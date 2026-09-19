package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class MapAnimV1(
    public val id: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val height: Int,
    public val packedDelay: Int,
    public val rotation: Int,
    public val unused0: Int,
    public val unused1: Int,
    public val unused2: Int,
) : IncomingServerGameMessage {
    public val delay: Int get() = packedDelay and 0x7FFF
    public val independentRotation: Boolean get() = packedDelay and 0x8000 != 0
}
