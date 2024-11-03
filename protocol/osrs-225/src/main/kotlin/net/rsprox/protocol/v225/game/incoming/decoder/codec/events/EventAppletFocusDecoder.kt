package net.rsprox.protocol.v225.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.events.EventAppletFocus
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class EventAppletFocusDecoder : ProxyMessageDecoder<EventAppletFocus> {
    override val prot: ClientProt = GameClientProt.EVENT_APPLET_FOCUS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventAppletFocus {
        val inFocus = buffer.g1() == 1
        return EventAppletFocus(inFocus)
    }
}
