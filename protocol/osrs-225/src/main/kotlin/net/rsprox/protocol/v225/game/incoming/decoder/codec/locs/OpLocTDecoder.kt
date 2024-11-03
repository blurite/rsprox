package net.rsprox.protocol.v225.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

internal class OpLocTDecoder : ProxyMessageDecoder<OpLocT> {
    override val prot: ClientProt = GameClientProt.OPLOCT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocT {
        val controlKey = buffer.g1Alt1() == 1
        val id = buffer.g2Alt3()
        val selectedObj = buffer.g2()
        val z = buffer.g2Alt1()
        val x = buffer.g2Alt1()
        val selectedCombinedId = buffer.gCombinedIdAlt1()
        val selectedSub = buffer.g2Alt1()
        return OpLocT(
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
