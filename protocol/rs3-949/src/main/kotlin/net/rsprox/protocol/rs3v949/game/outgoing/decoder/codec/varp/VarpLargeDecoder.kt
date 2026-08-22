package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.session.Session

internal class VarpLargeDecoder : ProxyMessageDecoder<VarpLarge> {
    override val prot: ClientProt = GameServerProt.VARP_LARGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpLarge {
        val b1 = buffer.g1()
        val b0 = buffer.g1()
        val b3 = buffer.g1()
        val b2 = buffer.g1()
        val value = (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
        val id = buffer.g2()
        return VarpLarge(
            id,
            value,
        )
    }
}
