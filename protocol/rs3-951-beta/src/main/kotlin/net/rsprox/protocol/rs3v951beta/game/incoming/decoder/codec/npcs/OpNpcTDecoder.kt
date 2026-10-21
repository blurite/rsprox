package net.rsprox.protocol.rs3v951beta.game.incoming.decoder.codec.npcs

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
        val selectedCombinedId = buffer.g4Alt1()
        val selectedSub = buffer.g2Alt3()
        val selectedObj = buffer.g3()
        val controlKey = buffer.g1()
        val index = buffer.g2Alt2()
        return OpNpcT(
            index,
            selectedObj,
            controlKey,
            selectedSub,
            selectedCombinedId,
        )
    }
}
