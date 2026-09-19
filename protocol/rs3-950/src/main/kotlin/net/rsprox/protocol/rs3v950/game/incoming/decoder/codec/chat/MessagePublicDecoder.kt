package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessagePublic
import net.rsprox.protocol.rs3v950.buffer.readNativeHuffman
import net.rsprox.protocol.session.Session

internal class MessagePublicDecoder(
    override val prot: ClientProt,
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessagePublic> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePublic {
        // Only the default 0/0 prefix is natively proven; retain both bytes without guessing their roles.
        val colourAndEffect = List(2) { buffer.g1() }
        val message = buffer.readNativeHuffman(huffmanCodec)
        return MessagePublic(
            colourAndEffect,
            message,
        )
    }
}
