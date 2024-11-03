package net.rsprox.protocol.v223.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.SetHeatmapEnabled
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class SetHeatmapEnabledDecoder : ProxyMessageDecoder<SetHeatmapEnabled> {
    override val prot: ClientProt = GameServerProt.SET_HEATMAP_ENABLED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetHeatmapEnabled {
        val enabled = buffer.gboolean()
        return SetHeatmapEnabled(enabled)
    }
}
