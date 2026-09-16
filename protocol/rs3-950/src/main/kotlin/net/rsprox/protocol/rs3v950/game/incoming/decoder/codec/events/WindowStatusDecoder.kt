package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.WindowStatus
import net.rsprox.protocol.session.Session

internal class WindowStatusDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<WindowStatus> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): WindowStatus {
        val mode = buffer.g1()
        val width = buffer.g2()
        val height = buffer.g2()
        val antialias = buffer.g1()
        return WindowStatus(
            mode,
            width,
            height,
            antialias,
        )
    }
}
