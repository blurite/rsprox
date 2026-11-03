package net.rsprox.protocol.v242.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLocV3
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v242.game.incoming.decoder.prot.GameClientProt

public class OpLoc1V3Decoder : ProxyMessageDecoder<OpLocV3> {
    override val prot: ClientProt = GameClientProt.OPLOC1_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocV3 {
        val subop = buffer.g1Alt3()
        val z = buffer.g2()
        val x = buffer.g2Alt3()
        val id = buffer.g3()
        val controlKey = buffer.g1() == 1
        return OpLocV3(
            id,
            x,
            z,
            controlKey,
            1,
            subop,
        )
    }
}
