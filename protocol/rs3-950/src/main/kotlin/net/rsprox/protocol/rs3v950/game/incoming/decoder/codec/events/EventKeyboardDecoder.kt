package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.EventKeyboard
import net.rsprox.protocol.session.Session

internal class EventKeyboardDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<EventKeyboard> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventKeyboard {
        require(buffer.readableBytes() > 0 && buffer.readableBytes() % 4 == 0) {
            "Keyboard payload must contain complete four-byte records"
        }
        val events =
            List(buffer.readableBytes() / 4) {
                EventKeyboard.Key(buffer.g1(), buffer.g3())
            }
        return EventKeyboard(
            events,
        )
    }
}
