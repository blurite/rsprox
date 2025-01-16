package net.rsprox.protocol.v228.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonD
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt

public class IfButtonDDecoder : ProxyMessageDecoder<IfButtonD> {
    override val prot: ClientProt = GameClientProt.IF_BUTTOND

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfButtonD {
        val selectedObj = buffer.g2()
        val targetSub = buffer.g2Alt2()
        val selectedSub = buffer.g2Alt2()
        val targetCombinedId = buffer.gCombinedId()
        val targetObj = buffer.g2()
        val selectedCombinedId = buffer.gCombinedId()
        return IfButtonD(
            selectedCombinedId,
            selectedSub,
            selectedObj,
            targetCombinedId,
            targetSub,
            targetObj,
        )
    }
}
