package net.rsprox.protocol.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.objs.OpObj
import net.rsprox.protocol.session.Session

public class OpObj1Decoder : ProxyMessageDecoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObj {
        val x = buffer.g2Alt2()
        val z = buffer.g2Alt1()
        val id = buffer.g2Alt3()
        val controlKey = buffer.g1Alt3() == 1
        return OpObj(
            id,
            x,
            z,
            controlKey,
            1,
        )
    }
}
