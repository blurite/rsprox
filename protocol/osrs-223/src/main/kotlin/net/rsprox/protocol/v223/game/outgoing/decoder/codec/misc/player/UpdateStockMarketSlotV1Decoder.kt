package net.rsprox.protocol.v223.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStockMarketSlotV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class UpdateStockMarketSlotV1Decoder : ProxyMessageDecoder<UpdateStockMarketSlotV1> {
    override val prot: ClientProt = GameServerProt.UPDATE_STOCKMARKET_SLOT_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStockMarketSlotV1 {
        val slot = buffer.g1()
        return when (val status = buffer.g1()) {
            0 -> {
                UpdateStockMarketSlotV1(
                    slot,
                    UpdateStockMarketSlotV1.ResetStockMarketSlot,
                )
            }
            else -> {
                val obj = buffer.g2()
                val price = buffer.g4()
                val count = buffer.g4()
                val completedCount = buffer.g4()
                val completedGold = buffer.g4()
                UpdateStockMarketSlotV1(
                    slot,
                    UpdateStockMarketSlotV1.SetStockMarketSlot(
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
