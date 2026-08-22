package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfCloseSub
import net.rsprox.protocol.session.Session

internal class IfCloseSubDecoder : ProxyMessageDecoder<IfCloseSub> {
    override val prot: ClientProt = GameServerProt.IF_CLOSESUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfCloseSub {
        val b0 = buffer.g1()
        val b1 = buffer.g1()
        val b2 = buffer.g1()
        val b3 = buffer.g1()
        val parentComponentHash = ((b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3).toLong() and 0xFFFFFFFFL
        return IfCloseSub(parentComponentHash)
    }
}
