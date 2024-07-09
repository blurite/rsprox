package net.rsprox.protocol.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.events.EventMouseScroll

@Consistent
public class EventMouseScrollDecoder : MessageDecoder<EventMouseScroll> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_SCROLL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): EventMouseScroll {
        val rotation = buffer.g2s()
        return EventMouseScroll(rotation)
    }
}
