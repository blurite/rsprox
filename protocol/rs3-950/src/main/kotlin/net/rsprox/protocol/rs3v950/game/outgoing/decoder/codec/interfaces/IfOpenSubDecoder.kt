package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubDecoder : ProxyMessageDecoder<IfOpenSub> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSub {
        val componentHash = buffer.g4().toLong() and 0xFFFFFFFFL
        buffer.skipRead(12)
        val childId = buffer.g2Alt3()
        val layer = buffer.g1Alt3()
        buffer.skipRead(4)
        return IfOpenSub(
            componentHash,
            childId,
            layer,
        )
    }
}
