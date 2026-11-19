package net.rsprox.protocol.v243.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.varp.VarpLong
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v243.game.outgoing.decoder.prot.GameServerProt

internal class VarpLongDecoder : ProxyMessageDecoder<VarpLong> {
    override val prot: ClientProt = GameServerProt.VARP_LONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpLong {
        val high = buffer.g4Alt2()
        val low = buffer.g4Alt2()
        val id = buffer.g2Alt1()
        return VarpLong(
            id,
            (high.toLong() shl 32) or (low.toLong() and 0xFFFFFFFFL),
        )
    }
}
