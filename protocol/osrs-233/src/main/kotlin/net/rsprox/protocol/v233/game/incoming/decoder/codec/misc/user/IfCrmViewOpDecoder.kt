package net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.IfCrmViewOp
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.incoming.decoder.prot.GameClientProt

public class IfCrmViewOpDecoder : ProxyMessageDecoder<IfCrmViewOp> {
    override val prot: ClientProt = GameClientProt.IF_CRMVIEW_OP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfCrmViewOp {
        val behaviour1 = buffer.g4Alt3()
        val behaviour3 = buffer.g4Alt1()
        val behaviour2 = buffer.g4Alt2()
        val sub = buffer.g2Alt3()
        val combinedId = buffer.gCombinedId()
        val serverTarget = buffer.g4Alt1()
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
