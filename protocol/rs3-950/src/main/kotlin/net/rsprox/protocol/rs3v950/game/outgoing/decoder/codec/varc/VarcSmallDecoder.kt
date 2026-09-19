package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcSmall
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarcSmallDecoder : ProxyMessageDecoder<VarcSmall> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARC_SMALL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcSmall {
        val id = buffer.g2Alt1()
        val value = buffer.g1sAlt3()
        return VarcSmall(
            id,
            value,
        )
    }
}
