package net.rsprox.protocol.v232.game.incoming.decoder.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.players.OpPlayerT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.incoming.decoder.prot.GameClientProt

public class OpPlayerTDecoder : ProxyMessageDecoder<OpPlayerT> {
    override val prot: ClientProt = GameClientProt.OPPLAYERT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpPlayerT {
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val selectedSub = buffer.g2Alt2()
        val index = buffer.g2Alt3()
        val selectedObj = buffer.g2Alt2()
        val controlKey = buffer.g1Alt1() == 1
        return OpPlayerT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
