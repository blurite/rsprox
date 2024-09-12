package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetHide
import net.rsprox.protocol.session.Session

public class IfSetHideDecoder : ProxyMessageDecoder<IfSetHide> {
    override val prot: ClientProt = GameServerProt.IF_SETHIDE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetHide {
        val hidden = buffer.g1Alt3() == 1
        val combinedId = buffer.gCombinedIdAlt2()
        return IfSetHide(
            combinedId.interfaceId,
            combinedId.componentId,
            hidden,
        )
    }
}
