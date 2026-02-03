package net.rsprox.protocol.v236.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetNpcHead
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class IfSetNpcHeadDecoder : ProxyMessageDecoder<IfSetNpcHead> {
    override val prot: ClientProt = GameServerProt.IF_SETNPCHEAD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetNpcHead {
        val combinedId = buffer.gCombinedIdAlt1()
        val npc = buffer.g2()
        return IfSetNpcHead(
            combinedId.interfaceId,
            combinedId.componentId,
            npc,
        )
    }
}
