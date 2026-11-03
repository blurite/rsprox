package net.rsprox.protocol.v242.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.locs.OpLoc6V2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v242.game.incoming.decoder.prot.GameClientProt

public class OpLoc6V2Decoder : ProxyMessageDecoder<OpLoc6V2> {
    override val prot: ClientProt = GameClientProt.OPLOC6_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpLoc6V2 {
        val id = buffer.g3Alt2()
        return OpLoc6V2(id)
    }
}
