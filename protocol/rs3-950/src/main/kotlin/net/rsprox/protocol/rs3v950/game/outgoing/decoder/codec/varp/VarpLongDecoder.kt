package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpLong
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarpLongDecoder : ProxyMessageDecoder<VarpLong> {
    override val prot: ClientProt = GameServerProt.VARP_LONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpLong {
        val high = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        val low = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        val value = (high shl 32) or low
        val id = buffer.g2()
        return VarpLong(
            id,
            value,
        )
    }
}
