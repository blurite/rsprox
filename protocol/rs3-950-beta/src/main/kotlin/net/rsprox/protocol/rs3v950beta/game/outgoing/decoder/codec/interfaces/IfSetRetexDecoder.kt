package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetRetex
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetRetexDecoder : ProxyMessageDecoder<IfSetRetex> {
    override val prot: ClientProt = GameServerProt.IF_SETRETEX

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetRetex {
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        val destination = buffer.g2()
        val index = buffer.g1Alt3()
        val source = buffer.g2Alt3()
        return IfSetRetex(destination, index, source, componentHash)
    }
}
