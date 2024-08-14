package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAngle
import net.rsprox.protocol.session.Session

public class IfSetAngleDecoder : ProxyMessageDecoder<IfSetAngle> {
    override val prot: ClientProt = GameServerProt.IF_SETANGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetAngle {
        val combinedId = buffer.gCombinedId()
        val angleX = buffer.g2Alt3()
        val angleY = buffer.g2()
        val zoom = buffer.g2()
        return IfSetAngle(
            combinedId.interfaceId,
            combinedId.componentId,
            angleX,
            angleY,
            zoom,
        )
    }
}
