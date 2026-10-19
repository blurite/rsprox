package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridMoveColumn
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridMoveColumnDecoder : ProxyMessageDecoder<TelemetryGridMoveColumn> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_MOVE_COLUMN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridMoveColumn {
        val destination = buffer.g1Alt1()
        val source = buffer.g1()
        val group = buffer.g1()
        return TelemetryGridMoveColumn(
            source,
            group,
            destination,
        )
    }
}
