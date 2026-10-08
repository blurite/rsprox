package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.LoyaltyUpdate
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LoyaltyUpdateDecoder : ProxyMessageDecoder<LoyaltyUpdate> {
    override val prot: ClientProt = GameServerProt.LOYALTY_UPDATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LoyaltyUpdate {
        val loyaltyPoints = buffer.g4Alt2()
        return LoyaltyUpdate(
            loyaltyPoints,
        )
    }
}
