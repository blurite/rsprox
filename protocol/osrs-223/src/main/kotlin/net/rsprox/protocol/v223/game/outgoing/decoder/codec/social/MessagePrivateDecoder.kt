package net.rsprox.protocol.v223.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivate
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class MessagePrivateDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessagePrivate> {
    override val prot: ClientProt = GameServerProt.MESSAGE_PRIVATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePrivate {
        val sender = buffer.gjstr()
        val worldId = buffer.g2()
        val worldMessageCounter = buffer.g3()
        val chatCrownType = buffer.g1()
        val message = huffmanCodec.decode(buffer)
        return MessagePrivate(
            sender,
            worldId,
            worldMessageCounter,
            chatCrownType,
            message,
        )
    }
}
