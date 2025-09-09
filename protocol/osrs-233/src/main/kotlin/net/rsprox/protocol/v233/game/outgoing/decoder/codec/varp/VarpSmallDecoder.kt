package net.rsprox.protocol.v233.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

internal class VarpSmallDecoder : ProxyMessageDecoder<VarpSmall> {
    override val prot: ClientProt = GameServerProt.VARP_SMALL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpSmall {
        val value = buffer.g1Alt2()
        val id = buffer.g2Alt1()
        return VarpSmall(
            id,
            value,
        )
    }
}
