package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddGroup
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridAddGroupDecoder : ProxyMessageDecoder<TelemetryGridAddGroup> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_ADD_GROUP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridAddGroup {
        val id = buffer.g4Alt2()
        val index = buffer.g1Alt1()
        return TelemetryGridAddGroup(
            id,
            index,
        )
    }
}
