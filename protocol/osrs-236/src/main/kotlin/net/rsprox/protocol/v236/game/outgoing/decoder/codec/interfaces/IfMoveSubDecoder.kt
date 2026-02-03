package net.rsprox.protocol.v236.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfMoveSub
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class IfMoveSubDecoder : ProxyMessageDecoder<IfMoveSub> {
    override val prot: ClientProt = GameServerProt.IF_MOVESUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfMoveSub {
        val destinationCombinedId = buffer.gCombinedIdAlt3()
        val sourceCombinedId = buffer.gCombinedIdAlt2()
        return IfMoveSub(
            sourceCombinedId.interfaceId,
            sourceCombinedId.componentId,
            destinationCombinedId.interfaceId,
            destinationCombinedId.componentId,
        )
    }
}
