package net.rsprox.protocol.v226.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.HideObjOps
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class HideObjOpsDecoder : ProxyMessageDecoder<HideObjOps> {
    override val prot: ClientProt = GameServerProt.HIDEOBJOPS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HideObjOps {
        val hidden = buffer.gboolean()
        return HideObjOps(hidden)
    }
}
