package net.rsprox.protocol.v234.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.incoming.decoder.prot.GameClientProt

public class IfButtonTDecoder : ProxyMessageDecoder<IfButtonT> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfButtonT {
        val targetSub = buffer.g2Alt1()
        val selectedSub = buffer.g2()
        val selectedCombinedId = buffer.gCombinedId()
        val targetCombinedId = buffer.gCombinedIdAlt1()
        val selectedObj = buffer.g2Alt3()
        val targetObj = buffer.g2Alt2()
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
