package net.rsprox.protocol.v239.game.incoming.decoder.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntityT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.incoming.decoder.prot.GameClientProt

public class OpWorldEntityTDecoder : ProxyMessageDecoder<OpWorldEntityT> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITYT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpWorldEntityT {
        val index = buffer.g2Alt2()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val selectedObj = buffer.g2Alt3()
        val selectedSub = buffer.g2Alt3()
        val controlKey = buffer.g1Alt2() == 1
        return OpWorldEntityT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
