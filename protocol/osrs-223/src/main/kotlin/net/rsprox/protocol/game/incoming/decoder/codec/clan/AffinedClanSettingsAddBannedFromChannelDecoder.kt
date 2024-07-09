package net.rsprox.protocol.game.incoming.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsAddBannedFromChannel

@Consistent
public class AffinedClanSettingsAddBannedFromChannelDecoder :
    MessageDecoder<AffinedClanSettingsAddBannedFromChannel> {
    override val prot: ClientProt = GameClientProt.AFFINEDCLANSETTINGS_ADDBANNED_FROMCHANNEL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): AffinedClanSettingsAddBannedFromChannel {
        val clanId = buffer.g1()
        val memberIndex = buffer.g2()
        val name = buffer.gjstr()
        return AffinedClanSettingsAddBannedFromChannel(
            name,
            clanId,
            memberIndex,
        )
    }
}
