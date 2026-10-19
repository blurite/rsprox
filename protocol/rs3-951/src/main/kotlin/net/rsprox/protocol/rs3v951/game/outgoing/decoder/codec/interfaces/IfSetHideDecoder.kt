package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetHide
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetHideDecoder : ProxyMessageDecoder<IfSetHide> {
    override val prot: ClientProt = GameServerProt.IF_SETHIDE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetHide {
        val hidden = buffer.g1Alt3() == 1
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        return IfSetHide(
            componentHash,
            hidden,
        )
    }
}
