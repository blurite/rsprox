package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateSiteSettings

@Consistent
public class UpdateSiteSettingsDecoder : MessageDecoder<UpdateSiteSettings> {
    override val prot: ClientProt = GameServerProt.UPDATE_SITESETTINGS

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): UpdateSiteSettings {
        val settings = buffer.gjstr()
        return UpdateSiteSettings(
            settings,
        )
    }
}
