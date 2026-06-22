package net.rsprox.protocol.v237.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.IfCrmViewOp
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.incoming.decoder.prot.GameClientProt

public class IfCrmViewOpDecoder : ProxyMessageDecoder<IfCrmViewOp> {
    override val prot: ClientProt = GameClientProt.IF_CRMVIEW_OP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfCrmViewOp {
        val sub = buffer.g2Alt3()
        val combinedId = buffer.gCombinedIdAlt3()
        val behaviour1 = buffer.g4Alt2()
        val behaviour2 = buffer.g4()
        val behaviour3 = buffer.g4Alt3()
        val serverTarget = buffer.g4()
        return IfCrmViewOp(
            serverTarget,
            combinedId,
            sub,
            behaviour1,
            behaviour2,
            behaviour3,
        )
    }
}
