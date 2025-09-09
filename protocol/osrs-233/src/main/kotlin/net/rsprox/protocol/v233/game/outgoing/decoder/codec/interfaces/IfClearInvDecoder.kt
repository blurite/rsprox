package net.rsprox.protocol.v233.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfClearInv
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

internal class IfClearInvDecoder : ProxyMessageDecoder<IfClearInv> {
    override val prot: ClientProt = GameServerProt.IF_CLEARINV

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfClearInv {
        val combinedId = buffer.gCombinedIdAlt2()
        return IfClearInv(
            combinedId.interfaceId,
            combinedId.componentId,
        )
    }
}
