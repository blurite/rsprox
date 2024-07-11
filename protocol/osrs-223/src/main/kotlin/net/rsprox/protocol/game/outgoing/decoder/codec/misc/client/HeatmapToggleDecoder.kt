package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.HeatmapToggle
import net.rsprox.protocol.session.Session

@Consistent
public class HeatmapToggleDecoder : ProxyMessageDecoder<HeatmapToggle> {
    override val prot: ClientProt = GameServerProt.HEATMAP_TOGGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HeatmapToggle {
        val enabled = buffer.gboolean()
        return HeatmapToggle(enabled)
    }
}
