package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetScrollPos
import net.rsprox.protocol.session.Session

public class IfSetScrollPosDecoder : ProxyMessageDecoder<IfSetScrollPos> {
    override val prot: ClientProt = GameServerProt.IF_SETSCROLLPOS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetScrollPos {
        val scrollPos = buffer.g2()
        val combinedId = buffer.gCombinedIdAlt2()
        return IfSetScrollPos(
            combinedId.interfaceId,
            combinedId.componentId,
            scrollPos,
        )
    }
}
