package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPosition
import net.rsprox.protocol.session.Session

public class IfSetPositionDecoder : ProxyMessageDecoder<IfSetPosition> {
    override val prot: ClientProt = GameServerProt.IF_SETPOSITION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPosition {
        val y = buffer.g2Alt1()
        val combinedId = buffer.gCombinedIdAlt1()
        val x = buffer.g2Alt1()
        return IfSetPosition(
            combinedId.interfaceId,
            combinedId.componentId,
            x,
            y,
        )
    }
}
