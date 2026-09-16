package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageClanchannel
import net.rsprox.protocol.rs3v950.buffer.readNativeHuffman
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessageClanchannelDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessageClanchannel> {
    override val prot: ClientProt = GameServerProt.MESSAGE_CLANCHANNEL

    override fun decode(buffer: JagByteBuf, session: Session): MessageClanchannel {
        val channel = buffer.g1s()
        val sender = buffer.readNativeString()
        val messageWorld = buffer.g2()
        val messageCounter = buffer.g3()
        val playerType = buffer.g1()
        val message = buffer.readNativeHuffman(huffmanCodec)
        return MessageClanchannel(
            channel,
            sender,
            messageWorld,
            messageCounter,
            playerType,
            message,
        )
    }
}
