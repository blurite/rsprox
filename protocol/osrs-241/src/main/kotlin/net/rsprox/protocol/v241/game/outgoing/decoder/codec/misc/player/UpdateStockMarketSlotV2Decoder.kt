package net.rsprox.protocol.v241.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStockMarketSlotV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v241.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class UpdateStockMarketSlotV2Decoder : ProxyMessageDecoder<UpdateStockMarketSlotV2> {
    override val prot: ClientProt = GameServerProt.UPDATE_STOCKMARKET_SLOT_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStockMarketSlotV2 {
        val slot = buffer.g1()
        val firstState = buffer.g1()
        // The native reset branch consumes only 28 of the declared 34 bytes.
        // Its six trailing bytes are not yet understood: do not invent a reset payload.
        check(firstState != 0) { "241 stockmarket V2 reset layout is not yet verified" }
        val escaped = firstState and 7 == 7
        val version = if (escaped) buffer.g1() else 1
        val status = if (escaped) buffer.g1() else firstState
        val obj = buffer.g2()
        val price = if (version >= 2) buffer.g8() else buffer.g4().toLong() and 0xFFFFFFFFL
        val count = buffer.g4()
        val completedCount = buffer.g4()
        val completedGold = if (version >= 2) buffer.g8() else buffer.g4().toLong() and 0xFFFFFFFFL
        val extensionLength = if (version >= 2) buffer.g4() else 0
        require(extensionLength in 0..buffer.readableBytes()) { "Invalid offer extension length: $extensionLength" }
        val extension = List(extensionLength) { buffer.g1() }
        return UpdateStockMarketSlotV2(
            slot,
            version,
            status,
            obj,
            price,
            count,
            completedCount,
            completedGold,
            extension,
        )
    }
}
