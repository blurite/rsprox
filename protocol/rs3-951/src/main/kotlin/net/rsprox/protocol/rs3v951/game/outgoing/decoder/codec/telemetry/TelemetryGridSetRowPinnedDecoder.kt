package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridSetRowPinned
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridSetRowPinnedDecoder : ProxyMessageDecoder<TelemetryGridSetRowPinned> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_SET_ROW_PINNED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridSetRowPinned {
        val row = buffer.g1Alt1()
        val enabled = buffer.g1() == 1
        val group = buffer.g1()
        return TelemetryGridSetRowPinned(
            group,
            row,
            enabled,
        )
    }
}
