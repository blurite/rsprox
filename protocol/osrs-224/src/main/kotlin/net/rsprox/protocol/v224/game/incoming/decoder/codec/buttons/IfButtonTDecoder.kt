package net.rsprox.protocol.v224.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonT
import net.rsprox.protocol.session.Session

public class IfButtonTDecoder : ProxyMessageDecoder<IfButtonT> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfButtonT {
        val selectedObj = buffer.g2Alt3()
        val selectedSub = buffer.g2()
        val targetSub = buffer.g2Alt1()
        val targetObj = buffer.g2()
        val targetCombinedId = buffer.gCombinedIdAlt3()
        val selectedCombinedId = buffer.gCombinedIdAlt1()
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
