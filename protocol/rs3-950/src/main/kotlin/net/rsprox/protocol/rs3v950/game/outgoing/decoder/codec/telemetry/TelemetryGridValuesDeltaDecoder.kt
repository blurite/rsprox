package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridValuesDelta
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridValuesDeltaDecoder : ProxyMessageDecoder<TelemetryGridValuesDelta> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_VALUES_DELTA

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridValuesDelta {
        val groups = mutableListOf<TelemetryGridValuesDelta.Group>()
        while (true) {
            val group = buffer.g1s()
            if (group == -1) break
            require(group >= 0) { "Invalid telemetry group index $group" }
            val rows = mutableListOf<TelemetryGridValuesDelta.Row>()
            while (true) {
                val row = buffer.g1s()
                if (row == -1) break
                require(row >= 0) { "Invalid telemetry row index $row" }
                val cells = mutableListOf<TelemetryGridValuesDelta.Cell>()
                while (true) {
                    val column = buffer.g1s()
                    if (column == -1) break
                    require(column >= 0) { "Invalid telemetry column index $column" }
                    cells += TelemetryGridValuesDelta.Cell(column, buffer.g4())
                }
                rows += TelemetryGridValuesDelta.Row(row, cells)
            }
            groups += TelemetryGridValuesDelta.Group(group, rows)
        }
        return TelemetryGridValuesDelta(groups)
    }
}
