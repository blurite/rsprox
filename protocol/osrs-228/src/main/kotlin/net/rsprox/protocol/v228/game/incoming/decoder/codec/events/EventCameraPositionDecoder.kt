package net.rsprox.protocol.v228.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.events.EventCameraPosition
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder

public class EventCameraPositionDecoder : ProxyMessageDecoder<EventCameraPosition> {
    override val prot: ClientProt = GameClientProt.EVENT_CAMERA_POSITION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventCameraPosition {
        val angleY = buffer.g2Alt2()
        val angleX = buffer.g2Alt2()
        return EventCameraPosition(
            angleX,
            angleY,
        )
    }
}
