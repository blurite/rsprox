package net.rsprox.protocol.game.incoming.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.clan.ClanSettingsFullRequest

@Consistent
public class ClanSettingsFullRequestDecoder : MessageDecoder<ClanSettingsFullRequest> {
    override val prot: ClientProt = GameClientProt.CLANSETTINGS_FULL_REQUEST

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ClanSettingsFullRequest {
        val clanId = buffer.g1s()
        return ClanSettingsFullRequest(clanId)
    }
}
