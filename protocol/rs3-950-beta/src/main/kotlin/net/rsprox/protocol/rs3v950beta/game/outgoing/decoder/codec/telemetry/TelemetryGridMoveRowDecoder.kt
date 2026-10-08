package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridMoveRow
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridMoveRowDecoder : ProxyMessageDecoder<TelemetryGridMoveRow> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_MOVE_ROW

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridMoveRow {
        val destination = buffer.g1Alt2()
        val group = buffer.g1Alt1()
        val source = buffer.g1Alt2()
        return TelemetryGridMoveRow(
            group,
            source,
            destination,
        )
    }
}
