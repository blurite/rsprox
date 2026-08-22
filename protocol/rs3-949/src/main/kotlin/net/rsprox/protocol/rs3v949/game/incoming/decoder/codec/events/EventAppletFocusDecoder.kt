package net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.incoming.model.events.EventAppletFocus
import net.rsprox.protocol.session.Session

internal class EventAppletFocusDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<EventAppletFocus> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventAppletFocus {
        val inFocus = buffer.g1() != 0
        return EventAppletFocus(inFocus)
    }
}
