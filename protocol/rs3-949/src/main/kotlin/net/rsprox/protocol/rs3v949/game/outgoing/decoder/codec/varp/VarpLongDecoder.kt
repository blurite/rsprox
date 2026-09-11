package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpLong
import net.rsprox.protocol.session.Session

internal class VarpLongDecoder : ProxyMessageDecoder<VarpLong> {
    override val prot: ClientProt = GameServerProt.VARP_LONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpLong {
        val id = buffer.g2()
        var value = 0L
        repeat(8) {
            value = (value shl 8) or (buffer.g1().toLong() and 0xFF)
        }
        return VarpLong(
            id,
            value,
        )
    }
}
