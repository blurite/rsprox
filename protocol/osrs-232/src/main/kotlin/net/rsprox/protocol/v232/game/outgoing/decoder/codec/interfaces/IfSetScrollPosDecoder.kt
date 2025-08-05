package net.rsprox.protocol.v232.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetScrollPos
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.outgoing.decoder.prot.GameServerProt

internal class IfSetScrollPosDecoder : ProxyMessageDecoder<IfSetScrollPos> {
    override val prot: ClientProt = GameServerProt.IF_SETSCROLLPOS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetScrollPos {
        val combinedId = buffer.gCombinedId()
        val scrollPos = buffer.g2()
        return IfSetScrollPos(
            combinedId.interfaceId,
            combinedId.componentId,
            scrollPos,
        )
    }
}
