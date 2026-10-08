package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddColumn
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridAddColumnDecoder : ProxyMessageDecoder<TelemetryGridAddColumn> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_ADD_COLUMN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridAddColumn {
        val group = buffer.g1Alt1()
        val id = buffer.g4()
        val index = buffer.g1Alt3()
        return TelemetryGridAddColumn(
            group,
            id,
            index,
        )
    }
}
