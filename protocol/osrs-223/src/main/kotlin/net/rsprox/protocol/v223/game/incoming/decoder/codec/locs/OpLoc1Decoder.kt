package net.rsprox.protocol.v223.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.incoming.decoder.prot.GameClientProt

internal class OpLoc1Decoder : ProxyMessageDecoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLoc {
        val id = buffer.g2()
        val x = buffer.g2Alt1()
        val z = buffer.g2Alt3()
        val controlKey = buffer.g1() == 1
        return OpLoc(
            id,
            x,
            z,
            controlKey,
            1,
        )
    }
}
