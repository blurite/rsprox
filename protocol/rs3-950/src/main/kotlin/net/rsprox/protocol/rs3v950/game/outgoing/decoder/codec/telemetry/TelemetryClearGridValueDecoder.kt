package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryClearGridValue
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryClearGridValueDecoder : ProxyMessageDecoder<TelemetryClearGridValue> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_CLEAR_GRID_VALUE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryClearGridValue {
        val group = buffer.g1()
        val column = buffer.g1Alt2()
        val row = buffer.g1()
        return TelemetryClearGridValue(
            group,
            column,
            row,
        )
    }
}
