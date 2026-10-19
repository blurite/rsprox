package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddColumn
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridAddColumnDecoder : ProxyMessageDecoder<TelemetryGridAddColumn> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_ADD_COLUMN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridAddColumn {
        val id = buffer.g4Alt3()
        val group = buffer.g1Alt1()
        val index = buffer.g1Alt2()
        return TelemetryGridAddColumn(
            group,
            id,
            index,
        )
    }
}
