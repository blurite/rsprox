package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.UrlOpen

@Consistent
public class UrlOpenDecoder : MessageDecoder<UrlOpen> {
    override val prot: ClientProt = GameServerProt.URL_OPEN

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): UrlOpen {
        // The URL is already decrypted at the proxy level
        // Additionally, any sensitive web tokens are erased at the proxy.
        val url = buffer.gjstr()
        return UrlOpen(
            url,
        )
    }
}
