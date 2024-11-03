package net.rsprox.protocol.v223.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.incoming.decoder.prot.GameClientProt

internal class OpObjTDecoder : ProxyMessageDecoder<OpObjT> {
    override val prot: ClientProt = GameClientProt.OPOBJT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObjT {
        val id = buffer.g2Alt3()
        val z = buffer.g2()
        val selectedSub = buffer.g2Alt3()
        val controlKey = buffer.g1() == 1
        val selectedCombinedId = buffer.gCombinedId()
        val selectedObj = buffer.g2Alt2()
        val x = buffer.g2()
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
