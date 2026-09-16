package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.AffinedClanSettingsAddBannedFromChannel
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class AffinedClanSettingsAddBannedFromChannelDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<AffinedClanSettingsAddBannedFromChannel> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AffinedClanSettingsAddBannedFromChannel {
        val channel = buffer.g1()
        val member = buffer.g2()
        val name = buffer.readNativeString()
        return AffinedClanSettingsAddBannedFromChannel(
            channel,
            member,
            name,
        )
    }
}
