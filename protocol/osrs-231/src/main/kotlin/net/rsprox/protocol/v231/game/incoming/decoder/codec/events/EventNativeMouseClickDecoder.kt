package net.rsprox.protocol.v231.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.events.EventNativeMouseClick
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.incoming.decoder.prot.GameClientProt

public class EventNativeMouseClickDecoder : ProxyMessageDecoder<EventNativeMouseClick> {
    override val prot: ClientProt = GameClientProt.EVENT_NATIVE_MOUSE_CLICK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventNativeMouseClick {
        val lastTransmittedMouseClick = buffer.g2Alt1()
        val packedCoord = buffer.g4Alt2()
        val code = buffer.g1Alt2()
        return EventNativeMouseClick(
            lastTransmittedMouseClick,
            code,
            packedCoord and 0xFFFF,
            packedCoord ushr 16 and 0xFFFF,
        )
    }
}
