package net.rsprox.protocol.v236.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class VarpSmallDecoder : ProxyMessageDecoder<VarpSmall> {
    override val prot: ClientProt = GameServerProt.VARP_SMALL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpSmall {
        val id = buffer.g2Alt1()
        val value = buffer.g1sAlt2()
        return VarpSmall(
            id,
            value,
        )
    }
}
