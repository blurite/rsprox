package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.EventCameraPosition
import net.rsprox.protocol.session.Session

internal class EventCameraPositionDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<EventCameraPosition> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventCameraPosition {
        val yaw = buffer.g2Alt3()
        val pitch = buffer.g2Alt1()
        return EventCameraPosition(
            yaw,
            pitch,
        )
    }
}
