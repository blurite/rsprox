package net.rsprox.protocol.v228.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class OpObjTDecoder : ProxyMessageDecoder<OpObjT> {
    override val prot: ClientProt = GameClientProt.OPOBJT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObjT {
        val x = buffer.g2()
        val z = buffer.g2Alt2()
        val id = buffer.g2Alt3()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val selectedObj = buffer.g2Alt1()
        val controlKey = buffer.g1() == 1
        val selectedSub = buffer.g2Alt2()
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
