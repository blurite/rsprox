package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfSetHide
import net.rsprox.protocol.session.Session

internal class IfSetHideDecoder : ProxyMessageDecoder<IfSetHide> {
    override val prot: ClientProt = GameServerProt.IF_SETHIDE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetHide {
        val b1 = buffer.g1()
        val b0 = buffer.g1()
        val b3 = buffer.g1()
        val b2 = buffer.g1()
        val componentHash = ((b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3).toLong() and 0xFFFFFFFFL
        val hidden = buffer.g1() == 0x81
        return IfSetHide(
            componentHash,
            hidden,
        )
    }
}
