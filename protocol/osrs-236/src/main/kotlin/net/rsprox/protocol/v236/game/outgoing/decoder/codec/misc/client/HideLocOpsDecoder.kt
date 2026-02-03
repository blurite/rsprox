package net.rsprox.protocol.v236.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.HideLocOps
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class HideLocOpsDecoder : ProxyMessageDecoder<HideLocOps> {
    override val prot: ClientProt = GameServerProt.HIDELOCOPS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HideLocOps {
        val hidden = buffer.gboolean()
        return HideLocOps(hidden)
    }
}
