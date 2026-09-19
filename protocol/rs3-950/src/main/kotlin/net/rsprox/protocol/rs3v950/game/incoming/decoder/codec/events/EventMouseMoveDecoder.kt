package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.EventMouseMove
import net.rsprox.protocol.session.Session

internal class EventMouseMoveDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<EventMouseMove> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventMouseMove {
        val meanRemainder = buffer.g1()
        val remainder = buffer.g1()
        val movements = buffer.readMouseMovements(nativeMouse = false)
        return EventMouseMove(
            meanRemainder,
            remainder,
            movements,
        )
    }
}
