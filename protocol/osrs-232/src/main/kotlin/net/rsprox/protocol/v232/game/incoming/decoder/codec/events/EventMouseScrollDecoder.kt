package net.rsprox.protocol.v232.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.events.EventMouseScroll
import net.rsprox.protocol.v232.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class EventMouseScrollDecoder : ProxyMessageDecoder<EventMouseScroll> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_SCROLL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventMouseScroll {
        val rotation = buffer.g2s()
        return EventMouseScroll(rotation)
    }
}
