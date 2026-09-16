package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfButtonT
import net.rsprox.protocol.session.Session

internal class IfButtonTDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IfButtonT> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfButtonT {
        val selectedSub = buffer.g2Alt2()
        val selectedObj = buffer.g3Alt2()
        val targetCombinedId = buffer.g4Alt2()
        val targetSub = buffer.g2()
        val selectedCombinedId = buffer.g4Alt2()
        val targetObj = buffer.g3Alt1()
        return IfButtonT(
            selectedSub,
            selectedObj,
            targetCombinedId,
            targetSub,
            selectedCombinedId,
            targetObj,
        )
    }
}
