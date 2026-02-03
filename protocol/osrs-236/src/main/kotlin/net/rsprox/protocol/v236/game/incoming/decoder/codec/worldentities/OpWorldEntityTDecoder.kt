package net.rsprox.protocol.v236.game.incoming.decoder.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntityT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class OpWorldEntityTDecoder : ProxyMessageDecoder<OpWorldEntityT> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITYT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpWorldEntityT {
        val selectedObj = buffer.g2Alt1()
        val controlKey = buffer.g1Alt2() == 1
        val selectedCombinedId = buffer.gCombinedIdAlt3()
        val index = buffer.g2Alt3()
        val selectedSub = buffer.g2Alt2()
        return OpWorldEntityT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
