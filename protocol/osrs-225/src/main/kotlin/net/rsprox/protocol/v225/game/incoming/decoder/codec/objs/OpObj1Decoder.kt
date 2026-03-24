package net.rsprox.protocol.v225.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.objs.OpObjV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

internal class OpObj1Decoder : ProxyMessageDecoder<OpObjV1> {
    override val prot: ClientProt = GameClientProt.OPOBJ1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObjV1 {
        val x = buffer.g2Alt2()
        val z = buffer.g2Alt1()
        val id = buffer.g2Alt3()
        val controlKey = buffer.g1Alt3() == 1
        return OpObjV1(
            id,
            x,
            z,
            controlKey,
            1,
        )
    }
}
