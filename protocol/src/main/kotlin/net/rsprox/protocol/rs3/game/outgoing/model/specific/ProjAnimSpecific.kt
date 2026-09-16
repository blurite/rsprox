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
    public val distance: Int,
    public val startX: Int,
    public val endTime: Int,
    public val slope: Int,
    public val source: Int,
    public val deltaX: Int,
    public val id: Int,
    public val level: Int,
) : IncomingServerGameMessage
