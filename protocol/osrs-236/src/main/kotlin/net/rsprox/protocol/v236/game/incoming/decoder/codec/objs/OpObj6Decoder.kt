package net.rsprox.protocol.v236.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.objs.OpObj6
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class OpObj6Decoder : ProxyMessageDecoder<OpObj6> {
    override val prot: ClientProt = GameClientProt.OPOBJ6

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObj6 {
        val id = buffer.g2()
        val z = buffer.g2Alt1()
        val x = buffer.g2Alt1()
        return OpObj6(
            id,
            x,
            z,
        )
    }
}
