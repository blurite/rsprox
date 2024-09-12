package net.rsprox.protocol.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.events.EventMouseClick
import net.rsprox.protocol.session.Session

@Consistent
public class EventMouseClickDecoder : ProxyMessageDecoder<EventMouseClick> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_CLICK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventMouseClick {
        val packed = buffer.g2()
        val rightClick = packed and 0x1 != 0
        val lastTransmittedMouseClick = packed ushr 1
        val x = buffer.g2()
        val y = buffer.g2()
        return EventMouseClick(
            lastTransmittedMouseClick,
            rightClick,
            x,
            y,
        )
    }
}
