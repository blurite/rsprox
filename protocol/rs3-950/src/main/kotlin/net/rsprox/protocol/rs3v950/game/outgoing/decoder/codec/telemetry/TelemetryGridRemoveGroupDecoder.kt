package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.telemetry

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridRemoveGroup
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class TelemetryGridRemoveGroupDecoder : ProxyMessageDecoder<TelemetryGridRemoveGroup> {
    override val prot: ClientProt = GameServerProt.TELEMETRY_GRID_REMOVE_GROUP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TelemetryGridRemoveGroup {
        val group = buffer.g1Alt2()
        return TelemetryGridRemoveGroup(
            group,
        )
    }
}
