package net.rsprox.protocol.game.incoming.decoder.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate
import net.rsprox.protocol.session.Session

@Consistent
public class MessagePrivateDecoder(
    private val huffman: HuffmanCodec,
) : ProxyMessageDecoder<MessagePrivate> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PRIVATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePrivate {
        val name = buffer.gjstr()
        val message = huffman.decode(buffer)
        return MessagePrivate(
            name,
            message,
        )
    }
}
