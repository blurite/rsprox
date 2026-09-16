package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.EventMouseClick
import net.rsprox.protocol.session.Session

internal class EventMouseClickDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<EventMouseClick> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventMouseClick {
        val packedPosition = buffer.g4()
        val buttonAndDelta = buffer.g2()
        return EventMouseClick(
            packedPosition,
            buttonAndDelta,
        )
    }
}
