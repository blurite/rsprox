package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridMoveRow
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridMoveRowDecoder : ProxyMessageDecoder<TelemetryGridMoveRow> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_MOVE_ROW

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridMoveRow {
        val group = buffer.g1Alt1()
        val source = buffer.g1()
        val destination = buffer.g1()
        return TelemetryGridMoveRow(
            group,
            source,
            destination,
        )
    }
}
