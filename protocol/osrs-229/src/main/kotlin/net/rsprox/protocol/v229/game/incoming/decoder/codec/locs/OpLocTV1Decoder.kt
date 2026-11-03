package net.rsprox.protocol.v229.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLocTV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.incoming.decoder.prot.GameClientProt

public class OpLocTV1Decoder : ProxyMessageDecoder<OpLocTV1> {
    override val prot: ClientProt = GameClientProt.OPLOCT_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocTV1 {
        val x = buffer.g2Alt3()
        val selectedSub = buffer.g2Alt2()
        val id = buffer.g2Alt2()
        val selectedObj = buffer.g2Alt2()
        val controlKey = buffer.g1Alt1() == 1
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val z = buffer.g2Alt1()
        return OpLocTV1(
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
