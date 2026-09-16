package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridFull
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridFullDecoder : ProxyMessageDecoder<TelemetryGridFull> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridFull {
        val groupCount = buffer.g1()
        require(groupCount <= buffer.readableBytes() / 6) { "Truncated telemetry group headers" }
        val groups = List(groupCount) {
            val id = buffer.g4()
            val rowCount = buffer.g1()
            require(rowCount <= 40 && rowCount <= buffer.readableBytes() / 4) { "Invalid telemetry row count" }
            val rowIds = List(rowCount) { buffer.g4() }
            val columnCount = buffer.g1()
            require(columnCount <= 8 && columnCount <= buffer.readableBytes() / 4) { "Invalid telemetry column count" }
            val columnIds = List(columnCount) { buffer.g4() }
            require(rowIds.distinct().size == rowCount && columnIds.distinct().size == columnCount) {
                "Duplicate telemetry row/column key"
            }
            val rows = List(rowCount) {
                val pin = buffer.g1s()
                val cells = List(columnCount) {
                    val present = buffer.g1()
                    TelemetryGridFull.Cell(present, if (present != 0) buffer.g4() else null)
                }
                TelemetryGridFull.Row(pin, cells)
            }
            TelemetryGridFull.Group(id, rowIds, columnIds, rows)
        }
        return TelemetryGridFull(groups)
    }
}
