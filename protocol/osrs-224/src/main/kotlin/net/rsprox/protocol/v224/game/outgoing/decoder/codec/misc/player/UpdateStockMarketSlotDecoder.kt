package net.rsprox.protocol.v224.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStockMarketSlot
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class UpdateStockMarketSlotDecoder : ProxyMessageDecoder<UpdateStockMarketSlot> {
    override val prot: ClientProt = GameServerProt.UPDATE_STOCKMARKET_SLOT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStockMarketSlot {
        val slot = buffer.g1()
        return when (val status = buffer.g1()) {
            0 -> {
                UpdateStockMarketSlot(
                    slot,
                    UpdateStockMarketSlot.ResetStockMarketSlot,
                )
            }
            else -> {
                val obj = buffer.g2()
                val price = buffer.g4()
                val count = buffer.g4()
                val completedCount = buffer.g4()
                val completedGold = buffer.g4()
                UpdateStockMarketSlot(
                    slot,
                    UpdateStockMarketSlot.SetStockMarketSlot(
                        status,
                        obj,
                        price,
                        count,
                        completedCount,
                        completedGold,
                    ),
                )
            }
        }
    }
}
