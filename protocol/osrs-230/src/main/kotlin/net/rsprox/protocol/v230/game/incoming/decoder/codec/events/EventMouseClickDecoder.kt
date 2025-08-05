package net.rsprox.protocol.v230.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.events.EventMouseClickV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v230.game.incoming.decoder.prot.GameClientProt

@Consistent
public class EventMouseClickDecoder : ProxyMessageDecoder<EventMouseClickV1> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_CLICK_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventMouseClickV1 {
        val packed = buffer.g2()
        val rightClick = packed and 0x1 != 0
        val lastTransmittedMouseClick = packed ushr 1
        val x = buffer.g2()
        val y = buffer.g2()
        return EventMouseClickV1(
            lastTransmittedMouseClick,
            rightClick,
            x,
            y,
        )
    }
}
