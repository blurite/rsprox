package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varbit

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.VarbitLarge
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarbitLargeDecoder : ProxyMessageDecoder<VarbitLarge> {
    override val prot: ClientProt = GameServerProt.VARBIT_LARGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarbitLarge {
        val value = buffer.g4()
        val id = buffer.g2Alt1()
        return VarbitLarge(
            id,
            value,
        )
    }
}
