package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varbit

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.VarbitSmall
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarbitSmallDecoder : ProxyMessageDecoder<VarbitSmall> {
    override val prot: ClientProt = GameServerProt.VARBIT_SMALL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarbitSmall {
        val value = buffer.g1Alt1()
        val id = buffer.g2Alt1()
        return VarbitSmall(
            id,
            value,
        )
    }
}
