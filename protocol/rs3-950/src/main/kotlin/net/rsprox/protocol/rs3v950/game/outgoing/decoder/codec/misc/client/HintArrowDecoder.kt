package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class HintArrowDecoder : ProxyMessageDecoder<HintArrow> {
    override val prot: ClientProt = GameServerProt.HINT_ARROW

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HintArrow {
        val header = buffer.g1()
        val kind = header and 31
        val payload =
            when (kind) {
                0 -> HintArrow.Clear(List(13) { buffer.g1() })
                1, 10 ->
                    HintArrow.Actor(
                        buffer.g1(),
                        buffer.g2(),
                        buffer.g2(),
                        List(4) { buffer.g1() },
                        buffer.g4(),
                    )
                in 2..6 ->
                    HintArrow.Location(
                        buffer.g1(),
                        buffer.g1(),
                        buffer.g2(),
                        buffer.g2(),
                        buffer.g1(),
                        buffer.g2(),
                        buffer.g4(),
                    )
                else -> HintArrow.Other(buffer.g1(), buffer.g4(), List(8) { buffer.g1() })
            }
        return HintArrow(header ushr 5, kind, payload)
    }
}
