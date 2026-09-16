package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageFriendchannel
import net.rsprox.protocol.rs3v950.buffer.readNativeHuffman
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessageFriendchannelDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessageFriendchannel> {
    override val prot: ClientProt = GameServerProt.MESSAGE_FRIENDCHANNEL

    override fun decode(buffer: JagByteBuf, session: Session): MessageFriendchannel {
        val alternateSenderFlag = buffer.g1()
        val sender = buffer.readNativeString()
        val alternateSender = if (alternateSenderFlag == 1) buffer.readNativeString() else null
        val channelName = buffer.readNativeString()
        val messageWorld = buffer.g2()
        val messageCounter = buffer.g3()
        val playerType = buffer.g1()
        val message = buffer.readNativeHuffman(huffmanCodec)
        return MessageFriendchannel(
            alternateSenderFlag,
            sender,
            alternateSender,
            channelName,
            messageWorld,
            messageCounter,
            playerType,
            message,
        )
    }
}
