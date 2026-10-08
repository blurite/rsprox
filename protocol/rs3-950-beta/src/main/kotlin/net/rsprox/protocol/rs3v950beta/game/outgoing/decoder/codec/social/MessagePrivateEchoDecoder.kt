package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePrivateEcho
import net.rsprox.protocol.rs3v950beta.buffer.readNativeHuffman
import net.rsprox.protocol.rs3v950beta.buffer.readNativeString
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessagePrivateEchoDecoder(
    private val huffmanCodec: HuffmanCodec?,
) : ProxyMessageDecoder<MessagePrivateEcho> {
    override val prot: ClientProt = GameServerProt.MESSAGE_PRIVATE_ECHO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePrivateEcho {
        val recipient = buffer.readNativeString()
        val message = buffer.readNativeHuffman(huffmanCodec)
        return MessagePrivateEcho(
            recipient,
            message,
        )
    }
}
