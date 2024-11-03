package net.rsprox.protocol.v224.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.clan.MessageClanChannelSystem
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class MessageClanChannelSystemDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessageClanChannelSystem> {
    override val prot: ClientProt = GameServerProt.MESSAGE_CLANCHANNEL_SYSTEM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageClanChannelSystem {
        val clanType = buffer.g1()
        val worldId = buffer.g2()
        val worldMessageCounter = buffer.g3()
        val message = huffmanCodec.decode(buffer)
        return MessageClanChannelSystem(
            clanType,
            worldId,
            worldMessageCounter,
            message,
        )
    }
}
