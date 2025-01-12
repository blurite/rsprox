package net.rsprox.protocol.v225.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.HiscoreRequest
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class HiscoreRequestDecoder : ProxyMessageDecoder<HiscoreRequest> {
    override val prot: ClientProt = GameClientProt.HISCORE_REQUEST

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HiscoreRequest {
        val requestId = buffer.g1()
        val type = buffer.g1()
        val name = buffer.gjstr()
        return HiscoreRequest(
            type,
            requestId,
            name,
        )
    }
}
