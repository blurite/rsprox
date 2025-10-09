package net.rsprox.protocol.v234.game.incoming.decoder.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntityT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.incoming.decoder.prot.GameClientProt

public class OpWorldEntityTDecoder : ProxyMessageDecoder<OpWorldEntityT> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITYT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpWorldEntityT {
        val selectedSub = buffer.g2Alt1()
        val selectedCombinedId = buffer.gCombinedIdAlt3()
        val index = buffer.g2Alt3()
        val controlKey = buffer.g1() == 1
        val selectedObj = buffer.g2Alt3()
        return OpWorldEntityT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
