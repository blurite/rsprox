package net.rsprox.protocol.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivateEcho
import net.rsprox.protocol.session.Session

@Consistent
public class MessagePrivateEchoDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessagePrivateEcho> {
    override val prot: ClientProt = GameServerProt.MESSAGE_PRIVATE_ECHO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePrivateEcho {
        val recipient = buffer.gjstr()
        val message = huffmanCodec.decode(buffer)
        return MessagePrivateEcho(
            recipient,
            message,
        )
    }
}
