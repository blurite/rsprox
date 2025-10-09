package net.rsprox.protocol.v234.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.incoming.decoder.prot.GameClientProt

public class OpLocTDecoder : ProxyMessageDecoder<OpLocT> {
    override val prot: ClientProt = GameClientProt.OPLOCT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocT {
        val id = buffer.g2Alt3()
        val x = buffer.g2Alt2()
        val selectedSub = buffer.g2()
        val z = buffer.g2Alt3()
        val controlKey = buffer.g1Alt2() == 1
        val selectedCombinedId = buffer.gCombinedIdAlt1()
        val selectedObj = buffer.g2Alt2()
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
