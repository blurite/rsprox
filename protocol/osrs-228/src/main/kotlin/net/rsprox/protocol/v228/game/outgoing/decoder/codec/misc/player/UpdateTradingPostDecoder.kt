package net.rsprox.protocol.v228.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateTradingPost
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class UpdateTradingPostDecoder : ProxyMessageDecoder<UpdateTradingPost> {
    override val prot: ClientProt = GameServerProt.UPDATE_TRADINGPOST

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateTradingPost {
        val reset = buffer.g1() == 0
        if (reset) {
            return UpdateTradingPost(UpdateTradingPost.ResetTradingPost)
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
                        UpdateTradingPost.TradingPostOffer(
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
        return UpdateTradingPost(
            UpdateTradingPost.SetTradingPostOfferList(
                age,
                obj,
                status,
                offers,
            ),
        )
    }
}
