package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcStrSmall
import net.rsprox.protocol.session.Session

internal class VarcStrSmallDecoder : ProxyMessageDecoder<VarcStrSmall> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARCSTR_SMALL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcStrSmall {
        val value = buffer.gjstr()
        val id = buffer.g2()
        return VarcStrSmall(
            id,
            value,
        )
    }
}

