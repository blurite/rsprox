package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.session.Session

internal class IfOpenSubDecoder : ProxyMessageDecoder<IfOpenSub> {
    override val prot: ClientProt = GameServerProt.IF_OPEN_SUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSub {
        buffer.skipRead(4)
        val layer = buffer.g1Alt3()
        buffer.skipRead(4)
        buffer.skipRead(4)
        val childId = buffer.g2()
        buffer.skipRead(4)
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFFFFFFL
        return IfOpenSub(
            componentHash,
            childId,
            layer,
        )
    }
}
