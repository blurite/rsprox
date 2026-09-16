package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfMoveSub
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfMoveSubDecoder : ProxyMessageDecoder<IfMoveSub> {
    override val prot: ClientProt = GameServerProt.IF_MOVESUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfMoveSub {
        val destination = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        val source = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        return IfMoveSub(
            destination,
            source,
        )
    }
}
