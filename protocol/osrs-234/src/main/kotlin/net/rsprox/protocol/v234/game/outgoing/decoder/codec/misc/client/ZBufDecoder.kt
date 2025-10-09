package net.rsprox.protocol.v234.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.ZBuf
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class ZBufDecoder : ProxyMessageDecoder<ZBuf> {
    override val prot: ClientProt = GameServerProt.ZBUF

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ZBuf {
        val enabled = buffer.gboolean()
        return ZBuf(enabled)
    }
}
