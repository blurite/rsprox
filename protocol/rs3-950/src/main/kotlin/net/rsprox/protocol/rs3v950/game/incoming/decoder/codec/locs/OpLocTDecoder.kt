package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.locs

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
        val controlKey = buffer.g1Alt2()
        val x = buffer.g2Alt2()
        val selectedSub = buffer.g2()
        val z = buffer.g2Alt3()
        val selectedObj = buffer.g3Alt1()
        val id = buffer.g4Alt1()
        val selectedCombinedId = buffer.g4Alt2()
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
