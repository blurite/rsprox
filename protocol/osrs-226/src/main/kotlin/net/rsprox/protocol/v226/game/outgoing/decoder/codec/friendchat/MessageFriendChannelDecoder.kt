package net.rsprox.protocol.v226.game.outgoing.decoder.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.Base37
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.friendchat.MessageFriendChannel
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class MessageFriendChannelDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessageFriendChannel> {
    override val prot: ClientProt = GameServerProt.MESSAGE_FRIENDCHANNEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageFriendChannel {
        val sender = buffer.gjstr()
        val channelName = Base37.decodeWithCase(buffer.g8())
        val worldId = buffer.g2()
        val worldMessageCounter = buffer.g3()
        val chatCrownType = buffer.g1()
        val message = huffmanCodec.decode(buffer)
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
