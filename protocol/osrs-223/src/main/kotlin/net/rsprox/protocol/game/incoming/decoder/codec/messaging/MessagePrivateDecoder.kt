package net.rsprox.protocol.game.incoming.decoder.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate

@Consistent
public class MessagePrivateDecoder : MessageDecoder<MessagePrivate> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PRIVATE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MessagePrivate {
        val name = buffer.gjstr()
        val huffman = tools.huffmanCodec.provide()
        val message = huffman.decode(buffer)
        return MessagePrivate(
            name,
            message,
        )
    }
}
