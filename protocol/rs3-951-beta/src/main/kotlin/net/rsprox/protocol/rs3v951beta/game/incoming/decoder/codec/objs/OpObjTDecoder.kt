package net.rsprox.protocol.rs3v951beta.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.session.Session

internal class OpObjTDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<OpObjT> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpObjT {
        val id = buffer.g3Alt3()
        val x = buffer.g2Alt3()
        val selectedObj = buffer.g3Alt3()
        val flags = buffer.g1Alt1()
        val selectedCombinedId = buffer.g4Alt3()
        val selectedSub = buffer.g2Alt2()
        val z = buffer.g2Alt3()
        return OpObjT(
            x,
            flags,
            selectedSub,
            selectedObj,
            z,
            selectedCombinedId,
            id,
        )
    }
}
