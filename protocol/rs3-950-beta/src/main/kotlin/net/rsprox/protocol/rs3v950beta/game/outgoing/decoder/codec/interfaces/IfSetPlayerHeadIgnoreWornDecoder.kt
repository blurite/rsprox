package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPlayerHeadIgnoreWorn
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPlayerHeadIgnoreWornDecoder : ProxyMessageDecoder<IfSetPlayerHeadIgnoreWorn> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERHEAD_IGNOREWORN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerHeadIgnoreWorn {
        val kitExtra = buffer.g2Alt2()
        val kitHigh = buffer.g2Alt1()
        val kitLow = buffer.g2Alt2()
        val componentHash = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        return IfSetPlayerHeadIgnoreWorn(
            kitLow,
            kitExtra,
            kitHigh,
            componentHash,
        )
    }
}
