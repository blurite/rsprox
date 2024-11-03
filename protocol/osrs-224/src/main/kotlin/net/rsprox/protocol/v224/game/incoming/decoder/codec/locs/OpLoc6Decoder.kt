package net.rsprox.protocol.v224.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.locs.OpLoc6
import net.rsprox.protocol.session.Session

public class OpLoc6Decoder : ProxyMessageDecoder<OpLoc6> {
    override val prot: ClientProt = GameClientProt.OPLOC6

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLoc6 {
        val id = buffer.g2Alt2()
        return OpLoc6(id)
    }
}
