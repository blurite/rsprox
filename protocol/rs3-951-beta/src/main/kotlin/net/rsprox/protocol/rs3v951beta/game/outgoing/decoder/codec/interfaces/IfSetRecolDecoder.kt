package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetRecol
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetRecolDecoder : ProxyMessageDecoder<IfSetRecol> {
    override val prot: ClientProt = GameServerProt.IF_SETRECOL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetRecol {
        val componentHash = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        val destination = buffer.g2Alt1()
        val source = buffer.g2Alt2()
        val index = buffer.g1Alt1()
        return IfSetRecol(index, componentHash, destination, source)
    }
}
