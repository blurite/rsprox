package net.rsprox.protocol.v235.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.ServerTickEnd
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class ServerTickEndDecoder : ProxyMessageDecoder<ServerTickEnd> {
    override val prot: ClientProt = GameServerProt.SERVER_TICK_END

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ServerTickEnd {
        return ServerTickEnd
    }
}
