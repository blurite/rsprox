package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Decoded wire fields; client scaling is not applied to the stored values. */
public data class MapProjAnimHalfsq(
    public val coordinate: Int,
    public val flags: Int,
    public val deltaX: Int,
    public val deltaZ: Int,
    public val source: Int,
    public val target: Int,
    public val id: Int,
    public val startHeight: Int,
    public val endHeight: Int,
    public val startTime: Int,
    public val endTime: Int,
    public val slope: Int,
    public val distance: Int,
) : IncomingServerGameMessage {
    public val xInZone: Int get() = (coordinate ushr 4) and 15
    public val zInZone: Int get() = coordinate and 15
}
