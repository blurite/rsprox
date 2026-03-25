package net.rsprox.protocol.v237.game.incoming.decoder.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.npcs.OpNpcV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.incoming.decoder.prot.GameClientProt

public class OpNpc1V2Decoder : ProxyMessageDecoder<OpNpcV2> {
    override val prot: ClientProt = GameClientProt.OPNPC1_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpcV2 {
        val index = buffer.g2Alt1()
        val controlKey = buffer.g1Alt2() == 1
        val subop = buffer.g1()
        return OpNpcV2(
            index,
            controlKey,
            1,
            subop,
        )
    }
}
