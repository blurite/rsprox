package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcBitSmall
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarcBitSmallDecoder : ProxyMessageDecoder<VarcBitSmall> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARCBIT_SMALL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcBitSmall {
        val value = buffer.g1sAlt3()
        val id = buffer.g2()
        return VarcBitSmall(
            id,
            value,
        )
    }
}
