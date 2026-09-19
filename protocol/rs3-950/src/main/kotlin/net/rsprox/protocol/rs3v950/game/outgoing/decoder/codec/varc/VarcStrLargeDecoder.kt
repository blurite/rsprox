package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcStrLarge
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarcStrLargeDecoder : ProxyMessageDecoder<VarcStrLarge> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARCSTR_LARGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcStrLarge {
        val value = buffer.gjstr()
        val id = buffer.g2Alt1()
        return VarcStrLarge(
            id = id,
            value = value,
        )
    }
}
