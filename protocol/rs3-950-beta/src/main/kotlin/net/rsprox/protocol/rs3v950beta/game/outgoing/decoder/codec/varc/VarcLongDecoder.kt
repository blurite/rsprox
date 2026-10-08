package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcLong
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarcLongDecoder : ProxyMessageDecoder<VarcLong> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARC_LONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcLong {
        val high = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        val low = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        val value = (high shl 32) or low
        val id = buffer.g2Alt3()
        return VarcLong(
            id,
            value,
        )
    }
}
