package net.rsprox.protocol.v225.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

internal class OpLoc3Decoder : ProxyMessageDecoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLoc {
        val z = buffer.g2()
        val id = buffer.g2Alt1()
        val controlKey = buffer.g1Alt3() == 1
        val x = buffer.g2Alt2()
        return OpLoc(
            id,
            x,
            z,
            controlKey,
            3,
        )
    }
}
