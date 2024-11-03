package net.rsprox.protocol.v225.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.clan.MessageClanChannel
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class MessageClanChannelDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessageClanChannel> {
    override val prot: ClientProt = GameServerProt.MESSAGE_CLANCHANNEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageClanChannel {
        val clanType = buffer.g1()
        val name = buffer.gjstr()
        val worldId = buffer.g2()
        val worldMessageCounter = buffer.g3()
        val chatCrownType = buffer.g1()
        val message = huffmanCodec.decode(buffer)
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
