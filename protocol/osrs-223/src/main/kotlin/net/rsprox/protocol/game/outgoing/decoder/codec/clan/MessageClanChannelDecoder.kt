package net.rsprox.protocol.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.clan.MessageClanChannel

@Consistent
public class MessageClanChannelDecoder : MessageDecoder<MessageClanChannel> {
    override val prot: ClientProt = GameServerProt.MESSAGE_CLANCHANNEL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MessageClanChannel {
        val clanType = buffer.g1()
        val name = buffer.gjstr()
        val worldId = buffer.g2()
        val worldMessageCounter = buffer.g3()
        val chatCrownType = buffer.g1()
        val message =
            tools.huffmanCodec
                .provide()
                .decode(buffer)
        return MessageClanChannel(
            clanType,
            name,
            worldId,
            worldMessageCounter,
            chatCrownType,
            message,
        )
    }
}
