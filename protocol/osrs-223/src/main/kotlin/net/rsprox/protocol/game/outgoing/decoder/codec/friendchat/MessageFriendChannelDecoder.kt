package net.rsprox.protocol.game.outgoing.decoder.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.Base37
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.friendchat.MessageFriendChannel

@Consistent
public class MessageFriendChannelDecoder : MessageDecoder<MessageFriendChannel> {
    override val prot: ClientProt = GameServerProt.MESSAGE_FRIENDCHANNEL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MessageFriendChannel {
        val sender = buffer.gjstr()
        val channelName = Base37.decodeWithCase(buffer.g8())
        val worldId = buffer.g2()
        val worldMessageCounter = buffer.g3()
        val chatCrownType = buffer.g1()
        val message =
            tools.huffmanCodec
                .provide()
                .decode(buffer)
        return MessageFriendChannel(
            sender,
            channelName,
            worldId,
            worldMessageCounter,
            chatCrownType,
            message,
        )
    }
}
