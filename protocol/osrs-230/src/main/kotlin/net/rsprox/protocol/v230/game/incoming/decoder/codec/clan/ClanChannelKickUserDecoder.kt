package net.rsprox.protocol.v230.game.incoming.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelKickUser
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v230.game.incoming.decoder.prot.GameClientProt

@Consistent
public class ClanChannelKickUserDecoder : ProxyMessageDecoder<ClanChannelKickUser> {
    override val prot: ClientProt = GameClientProt.CLANCHANNEL_KICKUSER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanChannelKickUser {
        val clanId = buffer.g1()
        val memberIndex = buffer.g2()
        val name = buffer.gjstr()
        return ClanChannelKickUser(
            name,
            clanId,
            memberIndex,
        )
    }
}
