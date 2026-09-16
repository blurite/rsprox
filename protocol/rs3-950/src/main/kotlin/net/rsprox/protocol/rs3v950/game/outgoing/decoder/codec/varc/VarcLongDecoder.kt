package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcLong
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarcLongDecoder : ProxyMessageDecoder<VarcLong> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARC_LONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcLong {
        val high = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        val low = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        val id = buffer.g2Alt1()
        return VarcLong(
            id = id,
            value = (high shl 32) or low,
        )
    }
}
