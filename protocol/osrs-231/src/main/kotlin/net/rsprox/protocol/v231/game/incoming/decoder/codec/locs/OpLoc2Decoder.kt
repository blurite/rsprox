package net.rsprox.protocol.v231.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.incoming.decoder.prot.GameClientProt

public class OpLoc2Decoder : ProxyMessageDecoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLoc {
        val z = buffer.g2Alt1()
        val id = buffer.g2Alt1()
        val controlKey = buffer.g1Alt2() == 1
        val x = buffer.g2Alt2()
        return OpLoc(
            id,
            x,
            z,
            controlKey,
            2,
        )
    }
}
