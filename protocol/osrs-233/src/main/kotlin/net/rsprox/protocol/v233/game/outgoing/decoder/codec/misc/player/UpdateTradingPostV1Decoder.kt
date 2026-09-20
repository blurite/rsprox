package net.rsprox.protocol.v233.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateTradingPostV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class UpdateTradingPostV1Decoder : ProxyMessageDecoder<UpdateTradingPostV1> {
    override val prot: ClientProt = GameServerProt.UPDATE_TRADINGPOST_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateTradingPostV1 {
        val reset = buffer.g1() == 0
        if (reset) {
            return UpdateTradingPostV1(UpdateTradingPostV1.ResetTradingPost)
        }
        val age = buffer.g8()
        val obj = buffer.g2()
        val status = buffer.g1() == 1
        val offerCount = buffer.g2()
        val offers =
            buildList {
                for (i in 0..<offerCount) {
                    val name = buffer.gjstr()
                    val previousName = buffer.gjstr()
                    val world = buffer.g2()
                    val time = buffer.g8()
                    val price = buffer.g4()
                    val count = buffer.g4()
                    add(
                        UpdateTradingPostV1.TradingPostOffer(
                            name,
                            previousName,
                            world,
                            time,
                            price,
                            count,
                        ),
                    )
                }
            }
        return UpdateTradingPostV1(
            UpdateTradingPostV1.SetTradingPostOfferList(
                age,
                obj,
                status,
                offers,
            ),
        )
    }
}
