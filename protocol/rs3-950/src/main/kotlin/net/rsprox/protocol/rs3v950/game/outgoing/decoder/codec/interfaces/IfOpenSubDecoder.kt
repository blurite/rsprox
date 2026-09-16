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
        val legacyWord0 = buffer.g4Alt2()
        val legacyWord1 = buffer.g4Alt3()
        val legacyWord2 = buffer.g4Alt3()
        val childId = buffer.g2Alt3()
        val layer = buffer.g1Alt3()
        val legacyWord3 = buffer.g4Alt2()
        return IfOpenSub(
            componentHash = componentHash,
            childId = childId,
            layer = layer,
            legacyWord0 = legacyWord0,
            legacyWord1 = legacyWord1,
            legacyWord2 = legacyWord2,
            legacyWord3 = legacyWord3,
        )
    }
}
