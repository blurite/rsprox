package net.rsprox.protocol.game.incoming.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsSetMutedFromChannel

@Consistent
public class AffinedClanSettingsSetMutedFromChannelDecoder :
    MessageDecoder<AffinedClanSettingsSetMutedFromChannel> {
    override val prot: ClientProt = GameClientProt.AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
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
