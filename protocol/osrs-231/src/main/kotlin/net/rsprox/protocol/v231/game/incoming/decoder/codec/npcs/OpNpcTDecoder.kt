package net.rsprox.protocol.v231.game.incoming.decoder.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.npcs.OpNpcT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.incoming.decoder.prot.GameClientProt

public class OpNpcTDecoder : ProxyMessageDecoder<OpNpcT> {
    override val prot: ClientProt = GameClientProt.OPNPCT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpcT {
        val selectedSub = buffer.g2Alt3()
        val selectedCombinedId = buffer.gCombinedId()
        val controlKey = buffer.g1Alt2() == 1
        val index = buffer.g2Alt2()
        val selectedObj = buffer.g2()
        return OpNpcT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
