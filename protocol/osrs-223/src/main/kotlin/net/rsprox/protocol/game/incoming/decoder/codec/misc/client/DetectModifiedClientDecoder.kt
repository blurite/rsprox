package net.rsprox.protocol.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.DetectModifiedClient

@Consistent
public class DetectModifiedClientDecoder : MessageDecoder<DetectModifiedClient> {
    override val prot: ClientProt = GameClientProt.DETECT_MODIFIED_CLIENT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): DetectModifiedClient {
        val code = buffer.g4()
        return DetectModifiedClient(code)
    }
}
