package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

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
        val y = buffer.g2Alt1()
        val x = buffer.g2Alt1()
        val code = buffer.g1Alt1()
        val lastTransmittedMouseClick = buffer.g2Alt2()
        return EventNativeMouseClick(
            lastTransmittedMouseClick = lastTransmittedMouseClick,
            code = code,
            x = x,
            y = y,
        )
    }
}
