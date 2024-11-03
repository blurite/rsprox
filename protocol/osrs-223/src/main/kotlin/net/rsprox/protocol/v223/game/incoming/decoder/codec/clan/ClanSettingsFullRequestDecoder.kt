package net.rsprox.protocol.v223.game.incoming.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.clan.ClanSettingsFullRequest
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class ClanSettingsFullRequestDecoder : ProxyMessageDecoder<ClanSettingsFullRequest> {
    override val prot: ClientProt = GameClientProt.CLANSETTINGS_FULL_REQUEST

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanSettingsFullRequest {
        val clanId = buffer.g1s()
        return ClanSettingsFullRequest(clanId)
    }
}
