package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.HideObjOps
import net.rsprox.protocol.session.Session

@Consistent
public class HideObjOpsDecoder : ProxyMessageDecoder<HideObjOps> {
    override val prot: ClientProt = GameServerProt.HIDEOBJOPS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HideObjOps {
        val hidden = buffer.gboolean()
        return HideObjOps(hidden)
    }
}
