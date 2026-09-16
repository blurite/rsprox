package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.PingStatistics
import net.rsprox.protocol.session.Session

internal class PingStatisticsDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<PingStatistics> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PingStatistics {
        val latency = buffer.g2Alt3()
        val reserved = buffer.g1()
        val fps = buffer.g1Alt1()
        return PingStatistics(
            latency,
            reserved,
            fps,
        )
    }
}
