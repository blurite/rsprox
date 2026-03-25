package net.rsprox.protocol.v227.game.incoming.decoder.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.npcs.OpNpcV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

internal class OpNpc3Decoder : ProxyMessageDecoder<OpNpcV1> {
    override val prot: ClientProt = GameClientProt.OPNPC3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpcV1 {
        val index = buffer.g2Alt1()
        val controlKey = buffer.g1Alt2() == 1
        return OpNpcV1(
            index,
            controlKey,
            3,
        )
    }
}
