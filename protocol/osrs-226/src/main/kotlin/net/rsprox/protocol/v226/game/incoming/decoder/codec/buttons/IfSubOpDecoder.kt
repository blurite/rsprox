package net.rsprox.protocol.v226.game.incoming.decoder.codec.buttons
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.buttons.IfSubOp
import net.rsprox.protocol.session.Session

@Consistent
public class IfSubOpDecoder : ProxyMessageDecoder<IfSubOp> {
    override val prot: ClientProt = GameClientProt.IF_SUBOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSubOp {
        val combinedId = buffer.gCombinedId()
        val sub = buffer.g2()
        val obj = buffer.g2()
        val subop = buffer.g1()
        val op = buffer.g1()
        return IfSubOp(
            combinedId,
            sub,
            obj,
            op + 1,
            subop,
        )
    }
}
