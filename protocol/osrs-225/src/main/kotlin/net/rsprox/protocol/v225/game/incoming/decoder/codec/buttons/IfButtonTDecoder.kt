package net.rsprox.protocol.v225.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

internal class IfButtonTDecoder : ProxyMessageDecoder<IfButtonT> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfButtonT {
        val targetSub = buffer.g2Alt3()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val targetObj = buffer.g2Alt3()
        val selectedSub = buffer.g2Alt3()
        val selectedObj = buffer.g2Alt1()
        val targetCombinedId = buffer.gCombinedIdAlt3()
        return IfButtonT(
            selectedCombinedId,
            selectedSub,
            selectedObj,
            targetCombinedId,
            targetSub,
            targetObj,
        )
    }
}
