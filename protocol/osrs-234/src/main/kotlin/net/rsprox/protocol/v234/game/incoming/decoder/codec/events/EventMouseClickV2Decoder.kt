package net.rsprox.protocol.v234.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.events.EventMouseClickV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.incoming.decoder.prot.GameClientProt

public class EventMouseClickV2Decoder : ProxyMessageDecoder<EventMouseClickV2> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_CLICK_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventMouseClickV2 {
        val x = buffer.g2Alt3()
        val packed = buffer.g2()
        val y = buffer.g2Alt1()
        val code = buffer.g1Alt2()
        val rightClick = packed and 0x1 != 0
        val lastTransmittedMouseClick = packed ushr 1
        return EventMouseClickV2(
            lastTransmittedMouseClick,
            code,
            rightClick,
            x,
            y,
        )
    }
}
