package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfCloseSub
import net.rsprox.protocol.session.Session

internal class IfCloseSubDecoder : ProxyMessageDecoder<IfCloseSub> {
    override val prot: ClientProt = GameServerProt.IF_CLOSE_SUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfCloseSub {
        val parentComponentHash = buffer.g4().toLong() and 0xFFFFFFFFL
        return IfCloseSub(parentComponentHash)
    }
}
