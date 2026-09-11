package net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.EventNativeMouseClick
import net.rsprox.protocol.session.Session

internal class EventNativeMouseClickDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<EventNativeMouseClick> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventNativeMouseClick {
        val code = buffer.g1()
        val lastTransmittedMouseClick = buffer.g2()
        val y = buffer.g2()
        val x = buffer.g2()
        return EventNativeMouseClick(lastTransmittedMouseClick, code, x, y)
    }
}
