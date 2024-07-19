package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.SiteSettings
import net.rsprox.protocol.session.Session

@Consistent
public class SiteSettingsDecoder : ProxyMessageDecoder<SiteSettings> {
    override val prot: ClientProt = GameServerProt.SITE_SETTINGS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SiteSettings {
        val settings = buffer.gjstr()
        return SiteSettings(
            settings,
        )
    }
}
