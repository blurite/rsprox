package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenTopDecoder : ProxyMessageDecoder<IfOpenTop> {
    override val prot: ClientProt = GameServerProt.IF_OPENTOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenTop {
        // The native handler skips these fixed spans; preserve bytes without inventing XTEA transforms.
        val interfaceId = buffer.g2Alt3()
        val reserved = List(17) { buffer.g1() }
        return IfOpenTop(interfaceId = interfaceId, reserved = reserved)
    }
}
