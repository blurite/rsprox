package net.rsprox.protocol.v237.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLocV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.incoming.decoder.prot.GameClientProt

public class OpLoc5V2Decoder : ProxyMessageDecoder<OpLocV2> {
    override val prot: ClientProt = GameClientProt.OPLOC5_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocV2 {
        val id = buffer.g2Alt1()
        val x = buffer.g2Alt3()
        val z = buffer.g2Alt3()
        val subop = buffer.g1Alt3()
        val controlKey = buffer.g1() == 1
        return OpLocV2(
            id,
            x,
            z,
            controlKey,
            5,
            subop,
        )
    }
}
