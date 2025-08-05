package net.rsprox.protocol.v232.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.client.RSevenStatus
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.incoming.decoder.prot.GameClientProt

@Consistent
public class RSevenStatusDecoder : ProxyMessageDecoder<RSevenStatus> {
    override val prot: ClientProt = GameClientProt.RSEVEN_STATUS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RSevenStatus {
        val packed = buffer.g1()
        return RSevenStatus(packed)
    }
}
