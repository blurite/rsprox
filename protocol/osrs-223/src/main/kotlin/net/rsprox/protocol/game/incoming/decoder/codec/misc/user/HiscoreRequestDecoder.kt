package net.rsprox.protocol.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.HiscoreRequest

@Consistent
public class HiscoreRequestDecoder : MessageDecoder<HiscoreRequest> {
    override val prot: ClientProt = GameClientProt.HISCORE_REQUEST

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): HiscoreRequest {
        val type = buffer.g1()
        val requestId = buffer.g1()
        val name = buffer.gjstr()
        return HiscoreRequest(
            type,
            requestId,
            name,
        )
    }
}
