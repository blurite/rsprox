package net.rsprox.protocol.game.incoming.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelFullRequest

@Consistent
public class ClanChannelFullRequestDecoder : MessageDecoder<ClanChannelFullRequest> {
    override val prot: ClientProt = GameClientProt.CLANCHANNEL_FULL_REQUEST

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ClanChannelFullRequest {
        val clanId = buffer.g1s()
        return ClanChannelFullRequest(clanId)
    }
}
