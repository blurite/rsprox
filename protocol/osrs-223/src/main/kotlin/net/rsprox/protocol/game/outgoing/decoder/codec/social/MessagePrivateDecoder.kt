package net.rsprox.protocol.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivate

@Consistent
public class MessagePrivateDecoder : MessageDecoder<MessagePrivate> {
    override val prot: ClientProt = GameServerProt.MESSAGE_PRIVATE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MessagePrivate {
        val sender = buffer.gjstr()
        val worldId = buffer.g2()
        val worldMessageCounter = buffer.g3()
        val chatCrownType = buffer.g1()
        val message =
            tools.huffmanCodec
                .provide()
                .decode(buffer)
        return MessagePrivate(
            sender,
            worldId,
            worldMessageCounter,
            chatCrownType,
            message,
        )
    }
}
