package net.rsprox.protocol.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.Idle

@Consistent
public class IdleDecoder : MessageDecoder<Idle> {
    override val prot: ClientProt = GameClientProt.IDLE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): Idle {
        return Idle
    }
}
