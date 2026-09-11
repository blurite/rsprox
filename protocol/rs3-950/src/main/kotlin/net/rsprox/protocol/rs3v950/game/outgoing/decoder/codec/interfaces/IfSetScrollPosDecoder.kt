package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetScrollPos
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetScrollPosDecoder : ProxyMessageDecoder<IfSetScrollPos> {
    override val prot: ClientProt = GameServerProt.IF_SETSCROLLPOS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetScrollPos {
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFFFFFFL
        val scrollPos = buffer.g2Alt1()
        return IfSetScrollPos(
            componentHash,
            scrollPos,
        )
    }
}
