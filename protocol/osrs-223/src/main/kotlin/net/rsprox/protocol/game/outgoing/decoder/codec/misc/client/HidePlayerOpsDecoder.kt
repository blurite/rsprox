package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.HidePlayerOps
import net.rsprox.protocol.session.Session

@Consistent
public class HidePlayerOpsDecoder : ProxyMessageDecoder<HidePlayerOps> {
    override val prot: ClientProt = GameServerProt.HIDEPLAYEROPS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HidePlayerOps {
        val hidden = buffer.gboolean()
        return HidePlayerOps(hidden)
    }
}
