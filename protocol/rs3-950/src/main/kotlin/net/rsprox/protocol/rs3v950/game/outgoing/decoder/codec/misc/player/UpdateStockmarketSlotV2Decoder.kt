package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.UpdateStockmarketSlotV2
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateStockmarketSlotV2Decoder : ProxyMessageDecoder<UpdateStockmarketSlotV2> {
    override val prot: ClientProt = GameServerProt.UPDATE_STOCKMARKET_SLOT_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStockmarketSlotV2 {
        val group = buffer.g1()
        val slot = buffer.g1()
        require(slot < 8) { "Invalid stock-market slot $slot" }
        val state = buffer.g1()
        if (state == 0) return UpdateStockmarketSlotV2(group, slot, state, null, List(33) { buffer.g1() })
        val version = if (state and 7 == 7) buffer.g1() else null
        val updatedState = if (version != null) buffer.g1() else null
        val objectId = if (version != null && version >= 3) buffer.g3() else buffer.g2()
        val wide = version != null && version >= 2
        val price = if (wide) buffer.g8() else buffer.g4().toLong() and 0xFFFF_FFFFL
        val quantity = buffer.g4()
        val completedQuantity = buffer.g4()
        val total = if (wide) buffer.g8() else buffer.g4().toLong() and 0xFFFF_FFFFL
        val extensionLength = if (wide) buffer.g4() else null
        val extensionCapacity = if (version == 2) 1 else 0
        if (extensionLength != null) {
            require(extensionLength in 0..extensionCapacity) { "Offer extension exceeds its fixed envelope" }
        }
        val extension = List(extensionLength ?: 0) { buffer.g1() }
        val reservedCount =
            when {
                version == null -> 15
                version < 2 -> 13
                version == 2 -> 1 - requireNotNull(extensionLength)
                else -> 0
            }
        val reserved = List(reservedCount) { buffer.g1() }
        return UpdateStockmarketSlotV2(
            group,
            slot,
            state,
            UpdateStockmarketSlotV2.Offer(
                version,
                updatedState,
                objectId,
                price,
                quantity,
                completedQuantity,
                total,
                extensionLength,
                extension,
            ),
            reserved,
        )
    }
}
