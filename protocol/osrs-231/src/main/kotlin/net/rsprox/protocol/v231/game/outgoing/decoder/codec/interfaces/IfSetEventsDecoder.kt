package net.rsprox.protocol.v231.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetEvents
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

internal class IfSetEventsDecoder : ProxyMessageDecoder<IfSetEvents> {
    override val prot: ClientProt = GameServerProt.IF_SETEVENTS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetEvents {
        val combinedId = buffer.gCombinedIdAlt3()
        val start = buffer.g2Alt2()
        val events = buffer.g4Alt1()
        val end = buffer.g2()
        return IfSetEvents(
            combinedId.interfaceId,
            combinedId.componentId,
            start,
            end,
            events,
        )
    }
}
