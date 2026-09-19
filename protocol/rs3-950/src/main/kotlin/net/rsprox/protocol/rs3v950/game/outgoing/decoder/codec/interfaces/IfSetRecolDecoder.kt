package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetRecol
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetRecolDecoder : ProxyMessageDecoder<IfSetRecol> {
    override val prot: ClientProt = GameServerProt.IF_SETRECOL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetRecol {
        val index = buffer.g1Alt3()
        val componentHash = buffer.g4().toLong() and 0xFFFF_FFFFL
        val destination = buffer.g2Alt3()
        val source = buffer.g2Alt2()
        return IfSetRecol(
            index,
            componentHash,
            destination,
            source,
        )
    }
}
