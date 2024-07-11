package net.rsprox.protocol.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.Timings
import net.rsprox.protocol.session.Session

@Consistent
public class TimingsDecoder : ProxyMessageDecoder<Timings> {
    override val prot: ClientProt = GameClientProt.TIMINGS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Timings {
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
        return Timings(
            connectionLostDuration,
            loginDuration,
            clientState,
            loginCount,
        )
    }
}
