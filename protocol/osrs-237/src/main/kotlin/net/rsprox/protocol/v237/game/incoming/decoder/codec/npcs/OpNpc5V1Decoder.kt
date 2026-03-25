package net.rsprox.protocol.v237.game.incoming.decoder.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.npcs.OpNpcV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.incoming.decoder.prot.GameClientProt

public class OpNpc5V1Decoder : ProxyMessageDecoder<OpNpcV1> {
    override val prot: ClientProt = GameClientProt.OPNPC5_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpcV1 {
        val index = buffer.g2Alt2()
        val controlKey = buffer.g1Alt3() == 1
        return OpNpcV1(
            index,
            controlKey,
            5,
        )
    }
}
