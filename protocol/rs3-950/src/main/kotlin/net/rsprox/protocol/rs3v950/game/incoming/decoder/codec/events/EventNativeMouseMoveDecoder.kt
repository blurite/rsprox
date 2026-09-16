package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.EventNativeMouseMove
import net.rsprox.protocol.session.Session

internal class EventNativeMouseMoveDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<EventNativeMouseMove> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventNativeMouseMove {
        val meanRemainder = buffer.g1()
        val remainder = buffer.g1()
        val movements = buffer.readMouseMovements(nativeMouse = true)
        return EventNativeMouseMove(
            meanRemainder,
            remainder,
            movements,
        )
    }
}
