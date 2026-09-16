package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.LocPrefetch
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LocPrefetchDecoder : ProxyMessageDecoder<LocPrefetch> {
    override val prot: ClientProt = GameServerProt.LOC_PREFETCH

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocPrefetch {
        val id = buffer.g4()
        val shape = buffer.g1()
        return LocPrefetch(id, shape, 0)
    }
}
