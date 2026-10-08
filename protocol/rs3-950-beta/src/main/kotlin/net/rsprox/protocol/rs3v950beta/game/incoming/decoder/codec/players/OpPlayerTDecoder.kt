package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.players.OpPlayerT
import net.rsprox.protocol.session.Session

internal class OpPlayerTDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<OpPlayerT> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpPlayerT {
        val controlKey = buffer.g1Alt1()
        val selectedCombinedId = buffer.g4Alt1()
        val index = buffer.g2Alt1()
        val selectedObj = buffer.g3Alt1()
        val selectedSub = buffer.g2Alt1()
        return OpPlayerT(
            controlKey,
            index,
            selectedSub,
            selectedObj,
            selectedCombinedId,
        )
    }
}
