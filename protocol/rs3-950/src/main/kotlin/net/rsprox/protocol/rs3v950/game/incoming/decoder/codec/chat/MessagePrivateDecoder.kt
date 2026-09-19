package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessagePrivate
import net.rsprox.protocol.rs3v950.buffer.readNativeHuffman
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class MessagePrivateDecoder(
    override val prot: ClientProt,
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessagePrivate> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePrivate {
        val recipient = buffer.readNativeString()
        val message = buffer.readNativeHuffman(huffmanCodec)
        return MessagePrivate(
            recipient,
            message,
        )
    }
}
