package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.SiteSettings
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

// OSRS's consistent string layout, also observed in live RS3 950 traffic.
// The native 950 handler ignores it; this is not a native-reader proof.
internal class SiteSettingsDecoder : ProxyMessageDecoder<SiteSettings> {
    override val prot: ClientProt = GameServerProt.UPDATE_SITESETTINGS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SiteSettings {
        return SiteSettings(buffer.gjstr())
    }
}
