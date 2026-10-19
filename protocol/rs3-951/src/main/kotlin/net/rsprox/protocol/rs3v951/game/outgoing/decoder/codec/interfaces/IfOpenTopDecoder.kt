package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenTopDecoder : ProxyMessageDecoder<IfOpenTop> {
    override val prot: ClientProt = GameServerProt.IF_OPENTOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenTop {
        // The native handler skips these fixed spans; preserve bytes without inventing XTEA transforms.
        val reserved0 = List(5) { buffer.g1() }
        val interfaceId = buffer.g2Alt2()
        val reserved1 = List(12) { buffer.g1() }
        return IfOpenTop(interfaceId = interfaceId, reserved = reserved0 + reserved1)
    }
}
