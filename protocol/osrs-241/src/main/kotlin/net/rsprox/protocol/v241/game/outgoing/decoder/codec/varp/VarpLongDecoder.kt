package net.rsprox.protocol.v241.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.varp.VarpLong
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v241.game.outgoing.decoder.prot.GameServerProt

internal class VarpLongDecoder : ProxyMessageDecoder<VarpLong> {
    override val prot: ClientProt = GameServerProt.VARP_LONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpLong {
        val high = buffer.g4Alt3()
        val low = buffer.g4Alt3()
        val id = buffer.g2()
        return VarpLong(
            id,
            (high.toLong() shl 32) or (low.toLong() and 0xFFFFFFFFL),
        )
    }
}
