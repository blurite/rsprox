package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.session.Session

internal class OpLocTDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<OpLocT> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocT {
        val x = buffer.g2Alt3()
        val controlKey = buffer.g1Alt1()
        val selectedSub = buffer.g2Alt3()
        val selectedCombinedId = buffer.g4Alt3()
        val z = buffer.g2()
        val selectedObj = buffer.g3Alt2()
        val id = buffer.g4Alt1()
        return OpLocT(
            controlKey,
            x,
            selectedSub,
            z,
            selectedObj,
            id,
            selectedCombinedId,
        )
    }
}
