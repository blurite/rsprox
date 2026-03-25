package net.rsprox.protocol.v237.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLocV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.incoming.decoder.prot.GameClientProt

public class OpLoc4V1Decoder : ProxyMessageDecoder<OpLocV1> {
    override val prot: ClientProt = GameClientProt.OPLOC4_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocV1 {
        val id = buffer.g2Alt3()
        val z = buffer.g2Alt1()
        val controlKey = buffer.g1() == 1
        val x = buffer.g2()
        return OpLocV1(
            id,
            x,
            z,
            controlKey,
            4,
        )
    }
}
