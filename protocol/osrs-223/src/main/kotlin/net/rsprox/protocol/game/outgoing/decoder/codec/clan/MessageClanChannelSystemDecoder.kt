package net.rsprox.protocol.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.clan.MessageClanChannelSystem

@Consistent
public class MessageClanChannelSystemDecoder : MessageDecoder<MessageClanChannelSystem> {
    override val prot: ClientProt = GameServerProt.MESSAGE_CLANCHANNEL_SYSTEM

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MessageClanChannelSystem {
        val clanType = buffer.g1()
        val worldId = buffer.g2()
        val worldMessageCounter = buffer.g3()
        val message =
            tools.huffmanCodec
                .provide()
                .decode(buffer)
        return MessageClanChannelSystem(
            clanType,
            worldId,
            worldMessageCounter,
            message,
        )
    }
}
