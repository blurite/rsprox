package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.HintArrow

@Consistent
public class HintArrowDecoder : MessageDecoder<HintArrow> {
    override val prot: ClientProt = GameServerProt.HINT_ARROW

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): HintArrow {
        return when (val type = buffer.g1()) {
            0 -> {
                buffer.skipRead(5)
                HintArrow(HintArrow.ResetHintArrow)
            }
            1 -> {
                val index = buffer.g2()
                buffer.skipRead(3)
                HintArrow(HintArrow.NpcHintArrow(index))
            }
            10 -> {
                val index = buffer.g2()
                buffer.skipRead(3)
                HintArrow(HintArrow.PlayerHintArrow(index))
            }
            in 2..6 -> {
                val positionId = buffer.g1()
                val x = buffer.g2()
                val z = buffer.g2()
                val height = buffer.g1()
                HintArrow(
                    HintArrow.TileHintArrow(
                        x,
                        z,
                        height,
                        positionId,
                    ),
                )
            }
            else -> error("Unknown hint arrow type: $type")
        }
    }
}
