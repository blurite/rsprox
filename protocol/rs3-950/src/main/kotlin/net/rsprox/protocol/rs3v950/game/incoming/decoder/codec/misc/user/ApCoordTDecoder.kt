package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.ApCoordT
import net.rsprox.protocol.session.Session

internal class ApCoordTDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ApCoordT> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ApCoordT {
        val selectedSub = buffer.g2()
        val selectedObj = buffer.g3Alt3()
        val selectedCombinedId = buffer.g4()
        val x = buffer.g2Alt2()
        val z = buffer.g2Alt1()
        return ApCoordT(
            selectedSub,
            selectedObj,
            selectedCombinedId,
            x,
            z,
        )
    }
}
