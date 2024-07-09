package net.rsprox.protocol.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.events.EventCameraPosition

public class EventCameraPositionDecoder : MessageDecoder<EventCameraPosition> {
    override val prot: ClientProt = GameClientProt.EVENT_CAMERA_POSITION

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): EventCameraPosition {
        val angleY = buffer.g2Alt2()
        val angleX = buffer.g2Alt3()
        return EventCameraPosition(
            angleX,
            angleY,
        )
    }
}
