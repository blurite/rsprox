package net.rsprox.protocol.rs3.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Decoded fields; angle 255 becomes -1, but native height/progress scaling is not applied. */
public data class MapProjAnimHalfsqV2(
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
    public val angle: Int,
    public val progress: Int,
    public val startOffset: ProjectileOffset,
    public val endOffset: ProjectileOffset,
) : IncomingServerGameMessage {
    public val xInZone: Int get() = (coordinate ushr 4) and 15
    public val zInZone: Int get() = coordinate and 15
    public val followTerrain: Boolean get() = flags and 1 != 0

    /**
     * Native start height is (raw / 4) * 4 when set, truncating division toward zero,
     * and raw * 4 otherwise. End height is always raw * 4. Both raw heights are signed.
     */
    public val fineStartHeight: Boolean get() = flags and 2 != 0
    public val unknownFlags: Int get() = flags and 0xFC
}
