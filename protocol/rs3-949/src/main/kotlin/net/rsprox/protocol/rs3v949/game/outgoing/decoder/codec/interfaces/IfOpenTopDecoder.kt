package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.session.Session

internal class IfOpenTopDecoder : ProxyMessageDecoder<IfOpenTop> {
    override val prot: ClientProt = GameServerProt.IF_OPENTOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenTop {
        buffer.skipRead(8)
        val loRaw = buffer.g1()
        val lo = (loRaw - 128) and 0xFF
        val hi = buffer.g1()
        val interfaceId = (hi shl 8) or lo
        buffer.skipRead(9)
        return IfOpenTop(interfaceId)
    }
}
