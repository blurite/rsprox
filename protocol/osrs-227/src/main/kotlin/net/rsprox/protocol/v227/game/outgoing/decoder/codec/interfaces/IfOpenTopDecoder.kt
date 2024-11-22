package net.rsprox.protocol.v227.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

internal class IfOpenTopDecoder : ProxyMessageDecoder<IfOpenTop> {
    override val prot: ClientProt = GameServerProt.IF_OPENTOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenTop {
        val interfaceId = buffer.g2Alt3()
        return IfOpenTop(
            interfaceId,
        )
    }
}
