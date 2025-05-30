package net.rsprox.protocol.v229.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.outgoing.decoder.prot.GameServerProt

internal class VarpLargeDecoder : ProxyMessageDecoder<VarpLarge> {
    override val prot: ClientProt = GameServerProt.VARP_LARGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpLarge {
        val value = buffer.g4()
        val id = buffer.g2()
        return VarpLarge(
            id,
            value,
        )
    }
}
