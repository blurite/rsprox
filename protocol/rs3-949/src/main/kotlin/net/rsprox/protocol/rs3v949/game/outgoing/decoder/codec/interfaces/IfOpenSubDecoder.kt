package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.session.Session

internal class IfOpenSubDecoder : ProxyMessageDecoder<IfOpenSub> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSub {
        buffer.skipRead(4)
        val layer = (128 - buffer.g1()) and 0xFF
        buffer.skipRead(4)
        buffer.skipRead(4)
        val childId = buffer.g2()
        buffer.skipRead(4)
        val b1 = buffer.g1()
        val b0 = buffer.g1()
        val b3 = buffer.g1()
        val b2 = buffer.g1()
        val componentHash = ((b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0).toLong() and 0xFFFFFFFFL
        return IfOpenSub(
            componentHash,
            childId,
            layer,
        )
    }
}
