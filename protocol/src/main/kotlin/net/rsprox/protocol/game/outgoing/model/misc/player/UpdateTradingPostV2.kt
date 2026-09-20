package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Trading-post offers with 64-bit prices; the packet carries no version byte. */
public data class UpdateTradingPostV2(public val update: OfferList?) : IncomingServerGameMessage {
    public data class OfferList(
        public val age: Long,
        public val obj: Int,
        public val status: Boolean,
        public val offers: List<Offer>,
    )

    public data class Offer(
        public val name: String,
        public val previousName: String,
        public val world: Int,
        public val time: Long,
        public val price: Long,
        public val count: Int,
    )
}
