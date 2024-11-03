package net.rsprox.protocol.v226.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class HintArrowDecoder : ProxyMessageDecoder<HintArrow> {
    override val prot: ClientProt = GameServerProt.HINT_ARROW

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
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
                val x = buffer.g2()
                val z = buffer.g2()
                val height = buffer.g1()
                HintArrow(
                    HintArrow.TileHintArrow(
                        x,
                        z,
                        height,
                        type,
                    ),
                )
            }
            else -> error("Unknown hint arrow type: $type")
        }
    }
}
