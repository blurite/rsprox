package net.rsprox.protocol.v226.game.incoming.decoder.codec.misc.user
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.IfCrmViewClick
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt

internal class IfCrmViewClickDecoder : ProxyMessageDecoder<IfCrmViewClick> {
    override val prot: ClientProt = GameClientProt.IF_CRMVIEW_CLICK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfCrmViewClick {
        val serverTarget = buffer.g4Alt1()
        val behaviour1 = buffer.g4()
        val sub = buffer.g2()
        val combinedId = buffer.gCombinedIdAlt2()
        val behaviour2 = buffer.g4Alt2()
        val behaviour3 = buffer.g4Alt1()
        return IfCrmViewClick(
            serverTarget,
            combinedId,
            sub,
            behaviour1,
            behaviour2,
            behaviour3,
        )
    }
}
