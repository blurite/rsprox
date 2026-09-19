package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ProjectileOffset

public data class ProjAnimSpecificV2(
    public val startX: Int,
    public val source: Int,
    public val endHeight: Int,
    public val startHeight: Int,
    public val deltaZ: Int,
    public val angle: Int,
    public val flags: Int,
    public val startZ: Int,
    public val progress: Int,
    public val endOffset: ProjectileOffset,
    public val id: Int,
    public val startTime: Int,
    public val startOffset: ProjectileOffset,
    public val endTime: Int,
    public val level: Int,
    public val target: Int,
    public val deltaX: Int,
) : IncomingServerGameMessage {
    public val followTerrain: Boolean get() = flags and 1 != 0

    /**
     * Native start height is (raw / 4) * 4 when set (division toward zero), raw * 4 otherwise.
     * End height is always raw * 4.
     */
    public val fineStartHeight: Boolean get() = flags and 2 != 0
    public val unknownFlags: Int get() = flags and 0xFC
}
