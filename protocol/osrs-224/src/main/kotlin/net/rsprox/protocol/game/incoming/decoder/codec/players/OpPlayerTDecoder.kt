package net.rsprox.protocol.game.incoming.decoder.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.players.OpPlayerT
import net.rsprox.protocol.session.Session

public class OpPlayerTDecoder : ProxyMessageDecoder<OpPlayerT> {
    override val prot: ClientProt = GameClientProt.OPPLAYERT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpPlayerT {
        val selectedObj = buffer.g2()
        val selectedSub = buffer.g2Alt3()
        val index = buffer.g2()
        val combinedId = buffer.gCombinedIdAlt3()
        val controlKey = buffer.g1Alt1() == 1
        return OpPlayerT(
            index,
            controlKey,
            combinedId,
            selectedSub,
            selectedObj,
        )
    }
}
