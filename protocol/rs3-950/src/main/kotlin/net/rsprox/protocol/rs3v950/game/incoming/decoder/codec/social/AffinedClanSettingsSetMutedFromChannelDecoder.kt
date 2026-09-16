package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.AffinedClanSettingsSetMutedFromChannel
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class AffinedClanSettingsSetMutedFromChannelDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<AffinedClanSettingsSetMutedFromChannel> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AffinedClanSettingsSetMutedFromChannel {
        val channel = buffer.g1()
        val member = buffer.g2()
        val muted = buffer.g1()
        val name = buffer.readNativeString()
        return AffinedClanSettingsSetMutedFromChannel(
            channel,
            member,
            muted,
            name,
        )
    }
}
