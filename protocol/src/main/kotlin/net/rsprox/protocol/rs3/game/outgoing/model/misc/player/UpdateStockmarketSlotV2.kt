package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Reserved octets have fixed, version-dependent lengths; native offer code does not interpret them. */
public data class UpdateStockmarketSlotV2(
    public val group: Int,
    public val slot: Int,
    public val state: Int,
    public val offer: Offer?,
    public val reserved: List<Int>,
) : IncomingServerGameMessage {
    /** Wide prices/totals retain all 64 bits; legacy values are widened as unsigned 32-bit words. */
    public data class Offer(
        public val version: Int?,
        public val updatedState: Int?,
        public val objectId: Int,
        public val price: Long,
        public val quantity: Int,
        public val completedQuantity: Int,
        public val total: Long,
        public val extensionLength: Int?,
        public val extension: List<Int>,
    )
}
