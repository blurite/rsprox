package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.map.LocPrefetch
import net.rsprox.protocol.session.Session

internal class LocPrefetchDecoder : ProxyMessageDecoder<LocPrefetch> {
    override val prot: ClientProt = GameServerProt.LOC_PREFETCH

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocPrefetch {
        val locId = buffer.g4()
        val shapeRot = buffer.g1()
        return LocPrefetch(
            locId,
            shapeRot,
        )
    }
}
