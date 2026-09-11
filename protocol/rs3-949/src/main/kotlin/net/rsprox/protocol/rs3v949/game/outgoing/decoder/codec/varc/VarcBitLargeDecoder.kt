package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcBitLarge
import net.rsprox.protocol.session.Session

internal class VarcBitLargeDecoder : ProxyMessageDecoder<VarcBitLarge> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARCBIT_LARGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcBitLarge {
        val value = buffer.g4Alt1()
        val id = buffer.g2Alt3()
        return VarcBitLarge(
            id,
            value,
        )
    }
}
