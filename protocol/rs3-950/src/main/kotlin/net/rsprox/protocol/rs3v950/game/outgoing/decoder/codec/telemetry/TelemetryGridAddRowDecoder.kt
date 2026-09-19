package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddRow
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridAddRowDecoder : ProxyMessageDecoder<TelemetryGridAddRow> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_ADD_ROW

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridAddRow {
        val group = buffer.g1Alt2()
        val id = buffer.g4Alt2()
        val index = buffer.g1Alt3()
        return TelemetryGridAddRow(
            group,
            id,
            index,
        )
    }
}
