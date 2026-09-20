package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Expanded stockmarket offer. Extension bytes are explicitly length-delimited by the native decoder. */
public data class UpdateStockMarketSlotV2(
    public val slot: Int,
    public val version: Int,
    public val status: Int,
    public val obj: Int,
    public val price: Long,
    public val count: Int,
    public val completedCount: Int,
    public val completedGold: Long,
    public val extension: List<Int>,
) : IncomingServerGameMessage
