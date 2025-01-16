package net.rsprox.protocol.v228.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.misc.client.ConnectionTelemetry
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ConnectionTelemetryDecoder : ProxyMessageDecoder<ConnectionTelemetry> {
    override val prot: ClientProt = GameClientProt.CONNECTION_TELEMETRY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ConnectionTelemetry {
        val connectionLostDuration = buffer.g2()
        val loginDuration = buffer.g2()
        val unusedDuration = buffer.g2()
        check(unusedDuration == 0) {
            "Unknown duration detected: $unusedDuration"
        }
        val clientState = buffer.g2()
        val unused1 = buffer.g2()
        check(unused1 == 0) {
            "Unused1 property value detected: $unused1"
        }
        val loginCount = buffer.g2()
        val unused2 = buffer.g2()
        check(unused2 == 0) {
            "Unused2 property value detected: $unused2"
        }
        return ConnectionTelemetry(
            connectionLostDuration,
            loginDuration,
            clientState,
            loginCount,
        )
    }
}
