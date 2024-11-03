package net.rsprox.protocol.v226.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.HideNpcOps
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class HideNpcOpsDecoder : ProxyMessageDecoder<HideNpcOps> {
    override val prot: ClientProt = GameServerProt.HIDENPCOPS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HideNpcOps {
        val hidden = buffer.gboolean()
        return HideNpcOps(hidden)
    }
}
