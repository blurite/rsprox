package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageClanchannelSystem
import net.rsprox.protocol.rs3v950.buffer.readNativeHuffman
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessageClanchannelSystemDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessageClanchannelSystem> {
    override val prot: ClientProt = GameServerProt.MESSAGE_CLANCHANNEL_SYSTEM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageClanchannelSystem {
        val channel = buffer.g1s()
        val messageWorld = buffer.g2()
        val messageCounter = buffer.g3()
        val message = buffer.readNativeHuffman(huffmanCodec)
        return MessageClanchannelSystem(
            channel,
            messageWorld,
            messageCounter,
            message,
        )
    }
}
