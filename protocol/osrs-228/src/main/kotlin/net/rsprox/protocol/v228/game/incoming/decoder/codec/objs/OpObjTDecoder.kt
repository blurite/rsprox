package net.rsprox.protocol.v228.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt

internal class OpObjTDecoder : ProxyMessageDecoder<OpObjT> {
    override val prot: ClientProt = GameClientProt.OPOBJT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObjT {
        val x = buffer.g2Alt3()
        val controlKey = buffer.g1Alt3() == 1
        val z = buffer.g2Alt2()
        val selectedSub = buffer.g2Alt3()
        val selectedCombinedId = buffer.gCombinedIdAlt1()
        val id = buffer.g2()
        val selectedObj = buffer.g2()
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
