package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Decoded wire fields; client scaling is not applied to the stored values. */
public data class MapProjAnimV2(
    public val coordinate: Int,
    public val deltaX: Int,
    public val deltaZ: Int,
    public val target: Int,
    public val id: Int,
    public val startHeight: Int,
    public val endHeight: Int,
    public val startTime: Int,
    public val endTime: Int,
    /** The native handler normalizes the wire value 255 to -1. */
    public val angle: Int,
    public val progress: Int,
    public val unused0: Int,
    public val unused1: Int,
    public val unused2: Int,
    public val startOffset: ProjectileOffset,
    public val endOffset: ProjectileOffset,
) : IncomingServerGameMessage {
    public val xInZone: Int get() = (coordinate ushr 3) and 7
    public val zInZone: Int get() = coordinate and 7
    public val followTerrain: Boolean get() = coordinate and 0x80 != 0
    public val unusedCoordinateBit: Boolean get() = coordinate and 0x40 != 0
}
