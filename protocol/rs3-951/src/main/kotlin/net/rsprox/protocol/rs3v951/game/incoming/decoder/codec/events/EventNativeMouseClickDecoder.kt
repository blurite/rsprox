package net.rsprox.protocol.rs3v951.game.incoming.decoder.codec.events

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
        val packedPosition = buffer.g4Alt3()
        val lastTransmittedMouseClick = buffer.g2Alt3()
        val code = buffer.g1()
        val x = packedPosition and 0xFFFF
        val y = packedPosition ushr 16
        return EventNativeMouseClick(
            lastTransmittedMouseClick = lastTransmittedMouseClick,
            code = code,
            x = x,
            y = y,
        )
    }
}
