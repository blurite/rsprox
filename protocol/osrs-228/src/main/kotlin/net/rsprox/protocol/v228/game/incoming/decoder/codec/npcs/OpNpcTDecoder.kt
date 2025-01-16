package net.rsprox.protocol.v228.game.incoming.decoder.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.npcs.OpNpcT
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class OpNpcTDecoder : ProxyMessageDecoder<OpNpcT> {
    override val prot: ClientProt = GameClientProt.OPNPCT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpcT {
        val controlKey = buffer.g1Alt3() == 1
        val index = buffer.g2Alt3()
        val selectedSub = buffer.g2Alt1()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val selectedObj = buffer.g2Alt3()
        return OpNpcT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
