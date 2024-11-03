package net.rsprox.protocol.v224.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

internal class IfOpenSubDecoder : ProxyMessageDecoder<IfOpenSub> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSub {
        val interfaceId = buffer.g2Alt3()
        val combinedId = buffer.gCombinedId()
        val type = buffer.g1Alt3()
        return IfOpenSub(
            combinedId.interfaceId,
            combinedId.componentId,
            interfaceId,
            type,
        )
    }
}
