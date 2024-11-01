package net.rsprox.protocol.game.incoming.decoder.codec.players
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.players.OpPlayerT
import net.rsprox.protocol.session.Session

public class OpPlayerTDecoder : ProxyMessageDecoder<OpPlayerT> {
    override val prot: ClientProt = GameClientProt.OPPLAYERT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpPlayerT {
        val combinedId = buffer.gCombinedIdAlt1()
        val index = buffer.g2Alt2()
        val selectedSub = buffer.g2()
        val selectedObj = buffer.g2()
        val controlKey = buffer.g1() == 1
        return OpPlayerT(
            index,
            controlKey,
            combinedId,
            selectedSub,
            selectedObj,
        )
    }
}
