package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubDecoder : ProxyMessageDecoder<IfOpenSub> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSub {
        // The native handler skips these fixed spans; preserve bytes without inventing XTEA transforms.
        val reserved0 = List(4) { buffer.g1() }
        val componentHash = buffer.g4().toLong() and 0xFFFF_FFFFL
        val reserved1 = List(4) { buffer.g1() }
        val childId = buffer.g2Alt1()
        val reserved2 = List(4) { buffer.g1() }
        val layer = buffer.g1()
        val reserved3 = List(4) { buffer.g1() }
        return IfOpenSub(componentHash, childId, layer, reserved = reserved0 + reserved1 + reserved2 + reserved3)
    }
}
