package net.rsprox.protocol.v228.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfMoveSub
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.outgoing.decoder.prot.GameServerProt

internal class IfMoveSubDecoder : ProxyMessageDecoder<IfMoveSub> {
    override val prot: ClientProt = GameServerProt.IF_MOVESUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfMoveSub {
        val sourceCombinedId = buffer.gCombinedIdAlt1()
        val destinationCombinedId = buffer.gCombinedId()
        return IfMoveSub(
            sourceCombinedId.interfaceId,
            sourceCombinedId.componentId,
            destinationCombinedId.interfaceId,
            destinationCombinedId.componentId,
        )
    }
}
