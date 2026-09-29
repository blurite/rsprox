package net.rsprox.protocol.v241.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateTradingPostV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v241.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class UpdateTradingPostV2Decoder : ProxyMessageDecoder<UpdateTradingPostV2> {
    override val prot: ClientProt = GameServerProt.UPDATE_TRADINGPOST_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateTradingPostV2 {
        val hasUpdate = buffer.g1() != 0
        if (!hasUpdate) {
            return UpdateTradingPostV2(null)
        }
        val age = buffer.g8()
        val obj = buffer.g2()
        val status = buffer.g1() == 1
        val offerCount = buffer.g2()
        val offers =
            List(offerCount) {
                val name = buffer.gjstr()
                val previousName = buffer.gjstr()
                val world = buffer.g2()
                val time = buffer.g8()
                val price = buffer.g8()
                val count = buffer.g4()
                UpdateTradingPostV2.Offer(
                    name,
                    previousName,
                    world,
                    time,
                    price,
                    count,
                )
            }
        return UpdateTradingPostV2(
            UpdateTradingPostV2.OfferList(
                age,
                obj,
                status,
                offers,
            ),
        )
    }
}
