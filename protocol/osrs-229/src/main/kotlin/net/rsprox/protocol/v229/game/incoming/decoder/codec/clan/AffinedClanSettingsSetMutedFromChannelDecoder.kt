package net.rsprox.protocol.v229.game.incoming.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsSetMutedFromChannel
import net.rsprox.protocol.v229.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class AffinedClanSettingsSetMutedFromChannelDecoder :
    ProxyMessageDecoder<AffinedClanSettingsSetMutedFromChannel> {
    override val prot: ClientProt = GameClientProt.AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AffinedClanSettingsSetMutedFromChannel {
        val clanId = buffer.g1()
        val memberIndex = buffer.g2()
        val muted = buffer.g1() == 1
        val name = buffer.gjstr()
        return AffinedClanSettingsSetMutedFromChannel(
            name,
            clanId,
            memberIndex,
            muted,
        )
    }
}
