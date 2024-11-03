package net.rsprox.protocol.v226.game.incoming.decoder.codec.buttons
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonD
import net.rsprox.protocol.session.Session

public class IfButtonDDecoder : ProxyMessageDecoder<IfButtonD> {
    override val prot: ClientProt = GameClientProt.IF_BUTTOND

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfButtonD {
        val selectedCombinedId = buffer.gCombinedIdAlt3()
        val targetCombinedId = buffer.gCombinedIdAlt2()
        val targetSub = buffer.g2Alt3()
        val selectedObj = buffer.g2Alt3()
        val targetObj = buffer.g2()
        val selectedSub = buffer.g2()
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
