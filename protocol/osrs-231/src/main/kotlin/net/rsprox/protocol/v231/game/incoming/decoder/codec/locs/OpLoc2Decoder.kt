package net.rsprox.protocol.v231.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLocV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.incoming.decoder.prot.GameClientProt

public class OpLoc2Decoder : ProxyMessageDecoder<OpLocV1> {
    override val prot: ClientProt = GameClientProt.OPLOC2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocV1 {
        val z = buffer.g2Alt1()
        val id = buffer.g2Alt1()
        val controlKey = buffer.g1Alt2() == 1
        val x = buffer.g2Alt2()
        return OpLocV1(
            id,
            x,
            z,
            controlKey,
            2,
        )
    }
}
