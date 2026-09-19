package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.npcs.OpNpcT
import net.rsprox.protocol.session.Session

internal class OpNpcTDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<OpNpcT> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpcT {
        val index = buffer.g2()
        val selectedObj = buffer.g3Alt1()
        val controlKey = buffer.g1Alt2()
        val selectedSub = buffer.g2Alt1()
        val selectedCombinedId = buffer.g4()
        return OpNpcT(
            index,
            selectedObj,
            controlKey,
            selectedSub,
            selectedCombinedId,
        )
    }
}
