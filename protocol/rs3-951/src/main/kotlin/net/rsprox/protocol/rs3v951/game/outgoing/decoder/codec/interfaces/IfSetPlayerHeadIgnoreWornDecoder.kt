package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPlayerHeadIgnoreWorn
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPlayerHeadIgnoreWornDecoder : ProxyMessageDecoder<IfSetPlayerHeadIgnoreWorn> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERHEAD_IGNOREWORN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerHeadIgnoreWorn {
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        val kitLow = buffer.g2()
        val kitExtra = buffer.g2Alt3()
        val kitHigh = buffer.g2Alt3()
        return IfSetPlayerHeadIgnoreWorn(
            kitLow,
            kitExtra,
            kitHigh,
            componentHash,
        )
    }
}
