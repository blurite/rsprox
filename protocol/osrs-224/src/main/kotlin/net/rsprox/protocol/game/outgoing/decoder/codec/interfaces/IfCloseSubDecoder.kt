package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfCloseSub
import net.rsprox.protocol.session.Session

@Consistent
public class IfCloseSubDecoder : ProxyMessageDecoder<IfCloseSub> {
    override val prot: ClientProt = GameServerProt.IF_CLOSESUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfCloseSub {
        val combinedId = buffer.gCombinedId()
        return IfCloseSub(
            combinedId.interfaceId,
            combinedId.componentId,
        )
    }
}
