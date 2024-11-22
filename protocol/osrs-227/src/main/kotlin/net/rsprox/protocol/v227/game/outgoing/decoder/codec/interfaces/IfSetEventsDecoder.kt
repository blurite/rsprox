package net.rsprox.protocol.v227.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetEvents
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

internal class IfSetEventsDecoder : ProxyMessageDecoder<IfSetEvents> {
    override val prot: ClientProt = GameServerProt.IF_SETEVENTS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetEvents {
        val events = buffer.g4Alt1()
        val combinedId = buffer.gCombinedIdAlt1()
        val end = buffer.g2Alt3()
        val start = buffer.g2Alt2()
        return IfSetEvents(
            combinedId.interfaceId,
            combinedId.componentId,
            start,
            end,
            events,
        )
    }
}
