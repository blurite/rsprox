package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.DebugServerTriggers
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class DebugServerTriggersDecoder : ProxyMessageDecoder<DebugServerTriggers> {
    override val prot: ClientProt = GameServerProt.DEBUG_SERVER_TRIGGERS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): DebugServerTriggers {
        val interfaceId = buffer.g2()
        val start = buffer.g2()
        val endExclusive = buffer.g2()
        val count = (endExclusive - start).coerceAtLeast(0)
        require(count <= buffer.readableBytes() / 3) { "Truncated debug trigger records" }
        // Native skips exactly three bytes per record; do not invent trigger IDs or parameters.
        val records = List(count) { DebugServerTriggers.Record(buffer.g1(), buffer.g1(), buffer.g1()) }
        return DebugServerTriggers(interfaceId, start, endExclusive, records)
    }
}
