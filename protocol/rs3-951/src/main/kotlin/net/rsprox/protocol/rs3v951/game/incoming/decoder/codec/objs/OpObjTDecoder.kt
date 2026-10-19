package net.rsprox.protocol.rs3v951.game.incoming.decoder.codec.objs

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
        val selectedObj = buffer.g3Alt1()
        val z = buffer.g2Alt1()
        val x = buffer.g2Alt3()
        val id = buffer.g3Alt2()
        val flags = buffer.g1Alt2()
        val selectedSub = buffer.g2Alt2()
        val selectedCombinedId = buffer.g4()
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
