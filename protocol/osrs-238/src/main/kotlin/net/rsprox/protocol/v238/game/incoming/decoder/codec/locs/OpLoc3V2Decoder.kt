package net.rsprox.protocol.v238.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLocV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v238.game.incoming.decoder.prot.GameClientProt

public class OpLoc3V2Decoder : ProxyMessageDecoder<OpLocV2> {
    override val prot: ClientProt = GameClientProt.OPLOC3_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLocV2 {
        val id = buffer.g2Alt1()
        val subop = buffer.g1Alt2()
        val x = buffer.g2Alt2()
        val z = buffer.g2Alt2()
        val controlKey = buffer.g1Alt2() == 1
        return OpLocV2(
            id,
            x,
            z,
            controlKey,
            3,
            subop,
        )
    }
}
