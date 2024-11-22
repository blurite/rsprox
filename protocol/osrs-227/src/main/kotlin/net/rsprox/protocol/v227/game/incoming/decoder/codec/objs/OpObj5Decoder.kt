package net.rsprox.protocol.v227.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.objs.OpObj
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

internal class OpObj5Decoder : ProxyMessageDecoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ5

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObj {
        val z = buffer.g2Alt2()
        val id = buffer.g2()
        val x = buffer.g2()
        val controlKey = buffer.g1Alt1() == 1
        return OpObj(
            id,
            x,
            z,
            controlKey,
            5,
        )
    }
}
