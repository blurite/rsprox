package net.rsprox.protocol.v227.game.incoming.decoder.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class MessagePrivateDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessagePrivate> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PRIVATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePrivate {
        val name = buffer.gjstr()
        val message = huffmanCodec.decode(buffer)
        return MessagePrivate(
            name,
            message,
        )
    }
}
