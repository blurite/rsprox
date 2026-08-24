package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfSetPosition
import net.rsprox.protocol.session.Session

internal class IfSetPositionDecoder : ProxyMessageDecoder<IfSetPosition> {
    override val prot: ClientProt = GameServerProt.IF_SET_POSITION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPosition {
        val y = buffer.g2Alt3()
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFFFFFFL
        val x = buffer.g2Alt2()
        return IfSetPosition(
            componentHash,
            x,
            y,
        )
    }
}
