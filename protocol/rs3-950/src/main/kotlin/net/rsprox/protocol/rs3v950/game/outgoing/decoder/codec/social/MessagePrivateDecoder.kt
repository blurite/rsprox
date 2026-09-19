package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePrivate
import net.rsprox.protocol.rs3v950.buffer.readNativeHuffman
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessagePrivateDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessagePrivate> {
    override val prot: ClientProt = GameServerProt.MESSAGE_PRIVATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePrivate {
        val alternateSenderFlag = buffer.g1()
        val sender = buffer.readNativeString()
        val alternateSender = if (alternateSenderFlag == 1) buffer.readNativeString() else null
        val messageWorld = buffer.g2()
        val messageCounter = buffer.g3()
        val playerType = buffer.g1()
        val message = buffer.readNativeHuffman(huffmanCodec)
        return MessagePrivate(
            alternateSenderFlag,
            sender,
            alternateSender,
            messageWorld,
            messageCounter,
            playerType,
            message,
        )
    }
}
