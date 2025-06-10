package net.rsprox.protocol.v231.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetEventsV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

internal class IfSetEventsV2Decoder : ProxyMessageDecoder<IfSetEventsV2> {
    override val prot: ClientProt = GameServerProt.IF_SETEVENTS_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetEventsV2 {
        val events1 = buffer.g4Alt3()
        val events2 = buffer.g4Alt3()
        val end = buffer.g2()
        val combinedId = buffer.gCombinedId()
        val start = buffer.g2()
        return IfSetEventsV2(
            combinedId.interfaceId,
            combinedId.componentId,
            start,
            end,
            events1,
            events2,
        )
    }
}
