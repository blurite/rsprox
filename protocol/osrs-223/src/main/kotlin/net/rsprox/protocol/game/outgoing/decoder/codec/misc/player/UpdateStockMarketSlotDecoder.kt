package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStockMarketSlot

@Consistent
public class UpdateStockMarketSlotDecoder : MessageDecoder<UpdateStockMarketSlot> {
    override val prot: ClientProt = GameServerProt.UPDATE_STOCKMARKET_SLOT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
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
