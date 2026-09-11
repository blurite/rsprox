package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPosition
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPositionDecoder : ProxyMessageDecoder<IfSetPosition> {
    override val prot: ClientProt = GameServerProt.IF_SETPOSITION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPosition {
        val y = buffer.g2sAlt1()
        val componentHash = buffer.g4().toLong() and 0xFFFFFFFFL
        val x = buffer.g2sAlt1()
        return IfSetPosition(
            componentHash,
            x,
            y,
        )
    }
}
