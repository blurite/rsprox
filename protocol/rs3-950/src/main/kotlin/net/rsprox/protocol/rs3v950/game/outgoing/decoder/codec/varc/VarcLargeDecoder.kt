package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcLarge
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarcLargeDecoder : ProxyMessageDecoder<VarcLarge> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARC_LARGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcLarge {
        val id = buffer.g2Alt2()
        val value = buffer.g4Alt3()
        return VarcLarge(
            id,
            value,
        )
    }
}

