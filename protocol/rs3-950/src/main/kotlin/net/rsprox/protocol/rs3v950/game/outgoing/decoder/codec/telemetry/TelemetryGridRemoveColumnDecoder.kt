package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridRemoveColumn
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridRemoveColumnDecoder : ProxyMessageDecoder<TelemetryGridRemoveColumn> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_REMOVE_COLUMN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridRemoveColumn {
        val column = buffer.g1Alt1()
        val group = buffer.g1Alt1()
        return TelemetryGridRemoveColumn(
            column,
            group,
        )
    }
}
