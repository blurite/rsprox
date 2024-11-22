package net.rsprox.protocol.v227.game.incoming.decoder.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.npcs.OpNpc6
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

internal class OpNpc6Decoder : ProxyMessageDecoder<OpNpc6> {
    override val prot: ClientProt = GameClientProt.OPNPC6

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpc6 {
        val id = buffer.g2Alt3()
        return OpNpc6(id)
    }
}
