package net.rsprox.protocol.v236.game.incoming.decoder.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntity6
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class OpWorldEntity6Decoder : ProxyMessageDecoder<OpWorldEntity6> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY6

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpWorldEntity6 {
        val id = buffer.g2()
        return OpWorldEntity6(id)
    }
}
