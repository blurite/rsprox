package net.rsprox.protocol.v224.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.UrlOpen
import net.rsprox.protocol.session.Session

@Consistent
public class UrlOpenDecoder : ProxyMessageDecoder<UrlOpen> {
    override val prot: ClientProt = GameServerProt.URL_OPEN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UrlOpen {
        // The URL is already decrypted at the proxy level
        // Additionally, any sensitive web tokens are erased at the proxy.
        val url = buffer.gjstr()
        return UrlOpen(
            url,
        )
    }
}
