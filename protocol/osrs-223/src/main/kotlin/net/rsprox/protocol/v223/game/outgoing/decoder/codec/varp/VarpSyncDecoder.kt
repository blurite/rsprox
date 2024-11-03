package net.rsprox.protocol.v223.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.varp.VarpSync
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class VarpSyncDecoder : ProxyMessageDecoder<VarpSync> {
    override val prot: ClientProt = GameServerProt.VARP_SYNC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpSync {
        return VarpSync
    }
}
