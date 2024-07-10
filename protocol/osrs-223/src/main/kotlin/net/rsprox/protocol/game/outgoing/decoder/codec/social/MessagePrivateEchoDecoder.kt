package net.rsprox.protocol.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivateEcho

@Consistent
public class MessagePrivateEchoDecoder : MessageDecoder<MessagePrivateEcho> {
    override val prot: ClientProt = GameServerProt.MESSAGE_PRIVATE_ECHO

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MessagePrivateEcho {
        val recipient = buffer.gjstr()
        val message =
            tools.huffmanCodec
                .provide()
                .decode(buffer)
        return MessagePrivateEcho(
            recipient,
            message,
        )
    }
}
