package net.rsprox.protocol.v224.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

public class VarpSmallDecoder : ProxyMessageDecoder<VarpSmall> {
    override val prot: ClientProt = GameServerProt.VARP_SMALL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpSmall {
        val value = buffer.g1()
        val id = buffer.g2()
        return VarpSmall(
            id,
            value,
        )
    }
}
