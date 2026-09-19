package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenTopDecoder : ProxyMessageDecoder<IfOpenTop> {
    override val prot: ClientProt = GameServerProt.IF_OPENTOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenTop {
        val interfaceId = buffer.g2Alt1()
        val legacyWord0 = buffer.g4()
        val legacyWord1 = buffer.g4Alt3()
        val legacyWord2 = buffer.g4()
        val unused = buffer.g1()
        val legacyWord3 = buffer.g4Alt3()
        return IfOpenTop(
            interfaceId = interfaceId,
            legacyWord0 = legacyWord0,
            legacyWord1 = legacyWord1,
            legacyWord2 = legacyWord2,
            unused = unused,
            legacyWord3 = legacyWord3,
        )
    }
}
