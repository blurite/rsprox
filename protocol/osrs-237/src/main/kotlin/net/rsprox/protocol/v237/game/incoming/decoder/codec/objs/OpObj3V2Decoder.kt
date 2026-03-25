package net.rsprox.protocol.v237.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.objs.OpObjV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.incoming.decoder.prot.GameClientProt

public class OpObj3V2Decoder : ProxyMessageDecoder<OpObjV2> {
    override val prot: ClientProt = GameClientProt.OPOBJ3_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObjV2 {
        val subop = buffer.g1Alt2()
        val controlKey = buffer.g1() == 1
        val x = buffer.g2Alt3()
        val z = buffer.g2Alt3()
        val id = buffer.g2Alt1()
        return OpObjV2(
            id,
            x,
            z,
            controlKey,
            3,
            subop,
        )
    }
}
