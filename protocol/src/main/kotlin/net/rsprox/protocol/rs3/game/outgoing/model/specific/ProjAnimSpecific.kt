package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class ProjAnimSpecific(
    public val target: Int,
    public val endHeight: Int,
    public val startZ: Int,
    public val deltaZ: Int,
    public val startTime: Int,
    public val startHeight: Int,
    public val flags: Int,
    public val progress: Int,
    public val startX: Int,
    public val endTime: Int,
    public val angle: Int,
    public val source: Int,
    public val deltaX: Int,
    public val id: Int,
    public val level: Int,
) : IncomingServerGameMessage {
    public val followTerrain: Boolean get() = flags and 1 != 0

    /** Native start height is raw * 4 when set, raw * 16 otherwise; end is raw * 16. Heights are signed. */
    public val fineStartHeight: Boolean get() = flags and 2 != 0
    public val unknownFlags: Int get() = flags and 0xFC
}
