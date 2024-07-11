package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateSiteSettings
import net.rsprox.protocol.session.Session

@Consistent
public class UpdateSiteSettingsDecoder : ProxyMessageDecoder<UpdateSiteSettings> {
    override val prot: ClientProt = GameServerProt.UPDATE_SITESETTINGS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateSiteSettings {
        val settings = buffer.gjstr()
        return UpdateSiteSettings(
            settings,
        )
    }
}
