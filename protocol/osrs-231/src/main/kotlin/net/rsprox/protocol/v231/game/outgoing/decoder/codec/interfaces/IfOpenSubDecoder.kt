package net.rsprox.protocol.v231.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

internal class IfOpenSubDecoder : ProxyMessageDecoder<IfOpenSub> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSub {
        val type = buffer.g1()
        val interfaceId = buffer.g2Alt2()
        val combinedId = buffer.gCombinedIdAlt3()
        return IfOpenSub(
            combinedId.interfaceId,
            combinedId.componentId,
            interfaceId,
            type,
        )
    }
}
