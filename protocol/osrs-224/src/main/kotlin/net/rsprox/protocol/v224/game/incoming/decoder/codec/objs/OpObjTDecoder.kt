package net.rsprox.protocol.v224.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.session.Session

public class OpObjTDecoder : ProxyMessageDecoder<OpObjT> {
    override val prot: ClientProt = GameClientProt.OPOBJT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObjT {
        val z = buffer.g2()
        val selectedObj = buffer.g2Alt1()
        val selectedSub = buffer.g2Alt3()
        val id = buffer.g2Alt3()
        val x = buffer.g2Alt1()
        val selectedCombinedId = buffer.gCombinedIdAlt1()
        val controlKey = buffer.g1() == 1
        return OpObjT(
            id,
            x,
            z,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
