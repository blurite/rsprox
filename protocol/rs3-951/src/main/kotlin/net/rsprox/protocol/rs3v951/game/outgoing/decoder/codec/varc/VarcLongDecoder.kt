package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcLong
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarcLongDecoder : ProxyMessageDecoder<VarcLong> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARC_LONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcLong {
        val id = buffer.g2()
        val high = buffer.g4().toLong() and 0xFFFF_FFFFL
        val low = buffer.g4().toLong() and 0xFFFF_FFFFL
        val value = (high shl 32) or low
        return VarcLong(
            id,
            value,
        )
    }
}
