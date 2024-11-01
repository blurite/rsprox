package net.rsprox.protocol.game.incoming.decoder.codec.npcs
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.npcs.OpNpcT
import net.rsprox.protocol.session.Session

public class OpNpcTDecoder : ProxyMessageDecoder<OpNpcT> {
    override val prot: ClientProt = GameClientProt.OPNPCT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OpNpcT {
        val controlKey = buffer.g1Alt2() == 1
        val selectedSub = buffer.g2Alt1()
        val selectedCombinedId = buffer.gCombinedId()
        val index = buffer.g2Alt3()
        val selectedObj = buffer.g2Alt3()
        return OpNpcT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
