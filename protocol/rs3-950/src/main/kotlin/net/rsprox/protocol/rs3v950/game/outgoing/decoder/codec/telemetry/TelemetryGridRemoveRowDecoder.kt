package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridRemoveRow
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridRemoveRowDecoder : ProxyMessageDecoder<TelemetryGridRemoveRow> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_REMOVE_ROW

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridRemoveRow {
        val group = buffer.g1Alt1()
        val row = buffer.g1Alt2()
        return TelemetryGridRemoveRow(
            group,
            row,
        )
    }
}
