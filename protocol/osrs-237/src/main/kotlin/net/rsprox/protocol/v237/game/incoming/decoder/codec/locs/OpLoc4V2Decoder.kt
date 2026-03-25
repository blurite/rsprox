package net.rsprox.protocol.v237.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLocV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.incoming.decoder.prot.GameClientProt

public class OpLoc4V2Decoder : ProxyMessageDecoder<OpLocV2> {
    override val prot: ClientProt = GameClientProt.OPLOC4_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocV2 {
        val subop = buffer.g1Alt2()
        val x = buffer.g2Alt2()
        val controlKey = buffer.g1Alt1() == 1
        val id = buffer.g2Alt3()
        val z = buffer.g2Alt2()
        return OpLocV2(
            id,
            x,
            z,
            controlKey,
            4,
            subop,
        )
    }
}
