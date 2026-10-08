package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcStrLarge
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarcStrLargeDecoder : ProxyMessageDecoder<VarcStrLarge> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARCSTR_LARGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcStrLarge {
        val id = buffer.g2()
        val value = buffer.gjstr()
        return VarcStrLarge(
            id = id,
            value = value,
        )
    }
}
