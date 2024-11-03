package net.rsprox.protocol.v224.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAnim
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

public class IfSetAnimDecoder : ProxyMessageDecoder<IfSetAnim> {
    override val prot: ClientProt = GameServerProt.IF_SETANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetAnim {
        val combinedId = buffer.gCombinedIdAlt3()
        val anim = buffer.g2Alt3()
        return IfSetAnim(
            combinedId.interfaceId,
            combinedId.componentId,
            anim,
        )
    }
}
