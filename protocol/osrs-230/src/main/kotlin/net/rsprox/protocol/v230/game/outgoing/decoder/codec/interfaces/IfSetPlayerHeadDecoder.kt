package net.rsprox.protocol.v230.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerHead
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v230.game.outgoing.decoder.prot.GameServerProt

internal class IfSetPlayerHeadDecoder : ProxyMessageDecoder<IfSetPlayerHead> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERHEAD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerHead {
        val combinedId = buffer.gCombinedIdAlt1()
        return IfSetPlayerHead(
            combinedId.interfaceId,
            combinedId.componentId,
        )
    }
}
